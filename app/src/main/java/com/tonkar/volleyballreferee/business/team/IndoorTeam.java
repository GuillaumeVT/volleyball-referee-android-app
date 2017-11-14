package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndoorTeam extends Team {

    private       int                   mLiberoColorId;
    private final Set<Integer>          mLiberos;
    private       boolean               mStartingLineupConfirmed;
    private final int                   mMaxSubstitutionsPerSet;
    private final Map<Integer, Integer> mSubstitutions;
    private       int                   mActingLibero;
    private final Set<Integer>          mMiddleBlockers;
    private       int                   mWaitingMiddleBlocker;

    public IndoorTeam(final TeamType teamType, int maxSubstitutionsPerSet) {
        super(teamType);
        mMaxSubstitutionsPerSet = maxSubstitutionsPerSet;
        mLiberoColorId = R.color.colorShirt1;
        mLiberos = new HashSet<>();
        mSubstitutions = new HashMap<>();
        mMiddleBlockers = new HashSet<>();
        putAllPlayersOnBench();
    }

    @Override
    protected Player createPlayer(int number) {
        return new IndoorPlayer(number);
    }

    @Override
    public boolean substitutePlayer(final int number, final PositionType positionType) {
        boolean result = false;

        if (isPossibleReplacement(number, positionType)) {
            result = super.substitutePlayer(number, positionType);
        }

        return result;
    }

    @Override
    protected void onSubstitution(int oldNumber, int newNumber, PositionType positionType) {
        if (isStartingLineupConfirmed()) {
            if (isLibero(newNumber)) {
                mActingLibero = newNumber;

                if (!isLibero(oldNumber)) {
                    mMiddleBlockers.clear();
                    mWaitingMiddleBlocker = oldNumber;
                    mMiddleBlockers.add(oldNumber);
                    mMiddleBlockers.add(getPlayerAtPosition(positionType.oppositePosition()));
                }
            } else if (isMiddleBlocker(newNumber) && hasWaitingMiddleBlocker() && isLibero(oldNumber)) {
                mWaitingMiddleBlocker = -1;
            } else {
                mSubstitutions.put(newNumber, oldNumber);

                if (isMiddleBlocker(oldNumber)) {
                    mMiddleBlockers.remove(oldNumber);
                    mMiddleBlockers.add(newNumber);
                }
            }
        }
    }

    public void putAllPlayersOnBench() {
        mStartingLineupConfirmed = false;
        mSubstitutions.clear();
        mActingLibero = -1;
        mMiddleBlockers.clear();
        mWaitingMiddleBlocker = -1;

        for (int number : getPlayers()) {
            super.substitutePlayer(number, PositionType.BENCH);
        }
    }

    public void confirmStartingLineup() {
        mStartingLineupConfirmed = true;
    }

    public boolean isStartingLineupConfirmed() {
        return mStartingLineupConfirmed;
    }

    public int getLiberoColorId() {
        return mLiberoColorId;
    }

    public void setLiberoColorId(int colorId) {
        mLiberoColorId = colorId;
    }

    public boolean isLibero(int number) {
        return mLiberos.contains(number);
    }

    public boolean canAddLibero() {
        return mLiberos.size() < 2;
    }

    public void addLibero(final int number) {
        if (canAddLibero() && hasPlayer(number)) {
            Log.i("VBR-Team", String.format("Add player #%d as libero of %s team", number, getTeamType().toString()));
            mLiberos.add(number);
        }
    }

    public void removeLibero(final int number) {
        if (hasPlayer(number) && isLibero(number)) {
            Log.i("VBR-Team", String.format("Remove player #%d as libero from %s team", number, getTeamType().toString()));
            mLiberos.remove(number);
        }
    }

    private boolean canSubstitute() {
        return mSubstitutions.size() < mMaxSubstitutionsPerSet;
    }

    public List<Integer> getPossibleReplacements(PositionType positionType) {
        List<Integer> availablePlayers = new ArrayList<>();

        // Can only do a fix number of substitutions
        if (canSubstitute()) {
            availablePlayers.addAll(getPossibleReplacementsNoMax(positionType));
        }

        return availablePlayers;
    }

    private List<Integer> getPossibleReplacementsNoMax(PositionType positionType) {
        List<Integer> availablePlayers = new ArrayList<>();

        // Once the starting line-up is confirmed, the rules must apply
        if (isStartingLineupConfirmed()) {
            int number = getPlayerAtPosition(positionType);

            if (number == mActingLibero) {
                // The acting libero can be replaced by the second libero or the middle blocker waiting outside
                if (hasSecondLibero()) {
                    availablePlayers.add(getSecondLibero());
                }
                if (hasWaitingMiddleBlocker()) {
                    availablePlayers.add(mWaitingMiddleBlocker);
                }
            } else {
                // A player who was replaced can only do one return trip with his regular replacement player
                if (hasReplacementPlayer(number)) {
                    if (canSubstitute(number)) {
                        availablePlayers.add(getReplacementPlayer(number));
                    }
                } else {
                    availablePlayers.addAll(getFreePlayersOnBench());
                    // The waiting middle blocker may be on the bench, but it should never be available
                    if (hasWaitingMiddleBlocker() && !hasReplacementPlayer(mWaitingMiddleBlocker)) {
                        availablePlayers.remove(availablePlayers.indexOf(mWaitingMiddleBlocker));
                    }
                }
                // If no libero is on the court, they can replace the player if he is at the back
                if (!hasLiberoOnCourt() && positionType.isAtTheBack()) {
                    availablePlayers.addAll(mLiberos);
                }
            }
        } else {
            availablePlayers.addAll(getFreePlayersOnBench());
        }

        return availablePlayers;
    }

    private boolean isPossibleReplacement(int number, PositionType positionType) {
        boolean result;

        if (involvesLibero(number, positionType)) {
            result = getPossibleReplacementsNoMax(positionType).contains(number);
        } else {
            result = getPossibleReplacements(positionType).contains(number);
        }

        return result;
    }

    private boolean involvesLibero(int number, PositionType positionType) {
        return isLibero(number) || isLibero(getPlayerAtPosition(positionType));
    }

    private boolean hasReplacementPlayer(int number) {
        return mSubstitutions.containsKey(number) || mSubstitutions.containsValue(number);
    }

    // A player can only do one return trip in each set
    private boolean canSubstitute(int number) {
        int count = 0;

        if (mSubstitutions.containsKey(number)) {
            count++;
        }
        if (mSubstitutions.containsValue(number)) {
            count++;
        }

        return count < 2;
    }

    private int getReplacementPlayer(int number) {
        int replacementNumber = -1;

        for (Map.Entry<Integer, Integer> entry : mSubstitutions.entrySet()) {
            if (entry.getKey() == number) {
                replacementNumber = entry.getValue();
            } else if (entry.getValue() == number) {
                replacementNumber = entry.getKey();
            }
        }

        return replacementNumber;
    }

    private List<Integer> getFreePlayersOnBench() {
        List<Integer> players = new ArrayList<>();

        for (int number : getPlayers()) {
            if (PositionType.BENCH.equals(getPlayerPosition(number)) && !hasReplacementPlayer(number) && !isLibero(number)) {
                players.add(number);
            }
        }

        return players;
    }

    private boolean hasLiberoOnCourt() {
        boolean result = false;

        for (int number : mLiberos) {
            if (!PositionType.BENCH.equals(getPlayerPosition(number))) {
                result = true;
            }
        }

        return result;
    }

    private boolean hasActingLibero() {
        return mActingLibero > 0;
    }

    private boolean hasSecondLibero() {
        return mLiberos.size() > 1;
    }

    private int getSecondLibero() {
        int secondLibero = -1;

        for (int number : mLiberos) {
            if (number != mActingLibero) {
                secondLibero = number;
            }
        }

        return secondLibero;
    }

    private boolean hasWaitingMiddleBlocker() {
        return mWaitingMiddleBlocker > 0;
    }

    private boolean isMiddleBlocker(int number) {
        return mMiddleBlockers.contains(number);
    }

    @Override
    public void rotateToNextPositions() {
        super.rotateToNextPositions();

        if (hasLiberoOnCourt() && hasWaitingMiddleBlocker() && isLibero(getPlayerAtPosition(PositionType.POSITION_4))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_4);
        }
    }

    @Override
    public void rotateToPreviousPositions() {
        super.rotateToPreviousPositions();

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && isLibero(getPlayerAtPosition(PositionType.POSITION_2))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_2);
        }
        if (hasActingLibero() && !hasLiberoOnCourt() && !hasWaitingMiddleBlocker() && isMiddleBlocker(getPlayerAtPosition(PositionType.POSITION_5))) {
            substitutePlayer(mActingLibero, PositionType.POSITION_5);
        }
    }

    public int checkPosition1Offence() {
        int middleBlockerNumber = -1;

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && isLibero(getPlayerAtPosition(PositionType.POSITION_1))) {
            middleBlockerNumber = mWaitingMiddleBlocker;
        }

        return middleBlockerNumber;
    }

    public int checkPosition1Defence() {
        int liberoNumber = -1;

        if (hasActingLibero() && !hasLiberoOnCourt() && !hasWaitingMiddleBlocker() && isMiddleBlocker(getPlayerAtPosition(PositionType.POSITION_1))) {
            liberoNumber = mActingLibero;
        }

        return liberoNumber;
    }

}
