package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.Substitution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class IndoorTeamComposition extends TeamComposition {

    @SerializedName("startingLineupConfirmed")
    private       boolean               mStartingLineupConfirmed;
    @SerializedName("startingLineup")
    private final Map<Integer, Player>  mStartingLineup;
    @SerializedName("maxSubstitutionsPerSet")
    private final int                   mMaxSubstitutionsPerSet;
    @SerializedName("substitutions")
    private final Map<Integer, Integer> mSubstitutions;
    @SerializedName("fullSubstitutions")
    private final List<Substitution>    mFullSubstitutions;
    @SerializedName("actingLibero")
    private       int                   mActingLibero;
    @SerializedName("middleBlockers")
    private final Set<Integer>          mMiddleBlockers;
    @SerializedName("waitingMiddleBlocker")
    private       int                   mWaitingMiddleBlocker;
    @SerializedName("actingCaptain")
    private       int                   mActingCaptain;

    public IndoorTeamComposition(final TeamDefinition teamDefinition, int maxSubstitutionsPerSet) {
        super(teamDefinition);

        mStartingLineupConfirmed = false;
        mStartingLineup = new LinkedHashMap<>();
        mMaxSubstitutionsPerSet = maxSubstitutionsPerSet;
        mSubstitutions = new LinkedHashMap<>();
        mFullSubstitutions = new ArrayList<>();
        mActingLibero = -1;
        mMiddleBlockers = new HashSet<>();
        mWaitingMiddleBlocker = -1;
        mActingCaptain = -1;
    }

    // For GSON Deserialization
    public IndoorTeamComposition() {
        this(new IndoorTeamDefinition(), 0);
    }

    @Override
    protected Player createPlayer(int number) {
        return new IndoorPlayer(number);
    }

    private IndoorTeamDefinition indoorTeamDefinition() {
        return (IndoorTeamDefinition) getTeamDefinition();
    }

    @Override
    public boolean substitutePlayer(final int number, final PositionType positionType, int homeTeamPoints, int guestTeamPoints) {
        boolean result = false;

        if (isPossibleSubstitution(number, positionType)) {
            result = super.substitutePlayer(number, positionType, homeTeamPoints, guestTeamPoints);
        }

        return result;
    }

    @Override
    protected void onSubstitution(int oldNumber, int newNumber, PositionType positionType, int homeTeamPoints, int guestTeamPoints) {
        Log.i("VBR-Team", String.format("Replacing player #%d by #%d for position %s of %s team", oldNumber, newNumber, positionType.toString(), indoorTeamDefinition().getTeamType().toString()));

        if (isStartingLineupConfirmed()) {
            if (indoorTeamDefinition().isLibero(newNumber)) {
                Log.i("VBR-Team", String.format("Player #%d of %s team is a libero and becomes acting libero", newNumber, indoorTeamDefinition().getTeamType().toString()));
                mActingLibero = newNumber;

                if (!indoorTeamDefinition().isLibero(oldNumber)) {
                    Log.i("VBR-Team", String.format("Player #%d of %s team is a middle blocker and is waiting outside", oldNumber, indoorTeamDefinition().getTeamType().toString()));
                    mMiddleBlockers.clear();
                    mWaitingMiddleBlocker = oldNumber;
                    mMiddleBlockers.add(oldNumber);
                    mMiddleBlockers.add(getPlayerAtPosition(positionType.oppositePosition()));
                }
            } else if (isMiddleBlocker(newNumber) && hasWaitingMiddleBlocker() && indoorTeamDefinition().isLibero(oldNumber)) {
                Log.i("VBR-Team", String.format("Player #%d of %s team is a middle blocker and is back on court", newNumber, indoorTeamDefinition().getTeamType().toString()));
                mWaitingMiddleBlocker = -1;
            } else {
                Log.i("VBR-Team", "Actual substitution");
                mSubstitutions.put(newNumber, oldNumber);
                mFullSubstitutions.add(new Substitution(newNumber, oldNumber, homeTeamPoints, guestTeamPoints));

                if (isMiddleBlocker(oldNumber)) {
                    Log.i("VBR-Team", String.format("Player #%d of %s team is a new middle blocker", newNumber, indoorTeamDefinition().getTeamType().toString()));
                    mMiddleBlockers.remove(oldNumber);
                    mMiddleBlockers.add(newNumber);
                }

                // A captain on the bench can no longer be acting captain
                if (indoorTeamDefinition().isCaptain(oldNumber) || isActingCaptain(oldNumber)) {
                    Log.i("VBR-Team", String.format("Player #%d acting captain of %s team leaves the court", oldNumber, indoorTeamDefinition().getTeamType().toString()));
                    mActingCaptain = -1;
                }

                // The game captain coming back on the court is automatically the captain again
                if (indoorTeamDefinition().isCaptain(newNumber)) {
                    Log.i("VBR-Team", String.format("Player #%d captain of %s team is back on court", newNumber, indoorTeamDefinition().getTeamType().toString()));
                    setActingCaptain(indoorTeamDefinition().getCaptain());
                }
            }
        }
    }

    public void confirmStartingLineup() {
        mStartingLineupConfirmed = true;

        for (int number : getPlayersOnCourt()) {
            Player player = createPlayer(number);
            player.setPosition(getPlayerPosition(number));
            mStartingLineup.put(number, player);
        }

        if (!PositionType.BENCH.equals(getPlayerPosition(indoorTeamDefinition().getCaptain()))) {
            setActingCaptain(indoorTeamDefinition().getCaptain());
        }
    }

    public boolean isStartingLineupConfirmed() {
        return mStartingLineupConfirmed;
    }

    private boolean canSubstitute() {
        return mSubstitutions.size() < mMaxSubstitutionsPerSet;
    }

    public Set<Integer> getPossibleSubstitutions(PositionType positionType) {
        Set<Integer> availablePlayers = new TreeSet<>(getPossibleSubstitutionsNoMax(positionType));
        Log.i("VBR-Team", String.format("Possible substitutions for position %s of %s team are %s", positionType.toString(), indoorTeamDefinition().getTeamType().toString(), availablePlayers.toString()));
        return availablePlayers;
    }

    private Set<Integer> getPossibleSubstitutionsNoMax(PositionType positionType) {
        Set<Integer> availablePlayers = new TreeSet<>();

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
                // Can only do a fix number of substitutions
                if (canSubstitute()) {
                    // A player who was replaced can only do one return trip with his regular replacement player
                    if (hasReplacementPlayer(number)) {
                        if (canSubstitute(number)) {
                            availablePlayers.add(getReplacementPlayer(number));
                        }
                    } else {
                        availablePlayers.addAll(getFreePlayersOnBench());
                        // The waiting middle blocker may be on the bench, but it should never be available
                        if (hasWaitingMiddleBlocker() && !hasReplacementPlayer(mWaitingMiddleBlocker)) {
                            availablePlayers.remove(mWaitingMiddleBlocker);
                        }
                    }
                }
                // If no libero is on the court, they can replace the player if he is at the back
                if (!hasLiberoOnCourt() && positionType.isAtTheBack()) {
                    availablePlayers.addAll(indoorTeamDefinition().getLiberos());
                }
            }
        } else {
            availablePlayers.addAll(getFreePlayersOnBench());
        }

        return availablePlayers;
    }

    private boolean isPossibleSubstitution(int number, PositionType positionType) {
        boolean result;

        if (PositionType.BENCH.equals(positionType) && !isStartingLineupConfirmed()) {
            result = true;
        } else if (involvesLibero(number, positionType)) {
            result = getPossibleSubstitutionsNoMax(positionType).contains(number);
        } else {
            result = getPossibleSubstitutions(positionType).contains(number);
        }

        return result;
    }

    private boolean involvesLibero(int number, PositionType positionType) {
        return indoorTeamDefinition().isLibero(number) || indoorTeamDefinition().isLibero(getPlayerAtPosition(positionType));
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

        for (int number : indoorTeamDefinition().getPlayers()) {
            if (PositionType.BENCH.equals(getPlayerPosition(number)) && !hasReplacementPlayer(number) && !indoorTeamDefinition().isLibero(number)) {
                players.add(number);
            }
        }

        return players;
    }

    private boolean hasLiberoOnCourt() {
        boolean result = false;

        for (int number : indoorTeamDefinition().getLiberos()) {
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
        return indoorTeamDefinition().getLiberos().size() > 1;
    }

    private int getSecondLibero() {
        int secondLibero = -1;

        for (int number : indoorTeamDefinition().getLiberos()) {
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

        if (hasLiberoOnCourt() && hasWaitingMiddleBlocker() && indoorTeamDefinition().isLibero(getPlayerAtPosition(PositionType.POSITION_4))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_4);
        }
    }

    @Override
    public void rotateToPreviousPositions() {
        super.rotateToPreviousPositions();

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && indoorTeamDefinition().isLibero(getPlayerAtPosition(PositionType.POSITION_2))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_2);
        }
        if (hasActingLibero() && !hasLiberoOnCourt() && !hasWaitingMiddleBlocker() && isMiddleBlocker(getPlayerAtPosition(PositionType.POSITION_5))) {
            substitutePlayer(mActingLibero, PositionType.POSITION_5);
        }
    }

    public int checkPosition1Offence() {
        int middleBlockerNumber = -1;

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && indoorTeamDefinition().isLibero(getPlayerAtPosition(PositionType.POSITION_1))) {
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

    public List<Substitution> getSubstitutions() {
        return new ArrayList<>(mFullSubstitutions);
    }

    public Set<Integer> getPlayersInStartingLineup() {
        return mStartingLineup.keySet();
    }

    public PositionType getPlayerPositionInStartingLineup(int number) {
        return mStartingLineup.get(number).getPosition();
    }

    public int getPlayerAtPositionInStartingLineup(PositionType positionType) {
        int number = -1;

        for (Player player : mStartingLineup.values()) {
            if (positionType.equals(player.getPosition())) {
                number = player.getNumber();
            }
        }
        return number;
    }

    public int getActingCaptain() {
        return mActingCaptain;
    }

    public void setActingCaptain(int number) {
        if (isStartingLineupConfirmed() && indoorTeamDefinition().hasPlayer(number) && !indoorTeamDefinition().isLibero(number)) {
            Log.i("VBR-Team", String.format("Player #%d of %s team is now acting captain", number, indoorTeamDefinition().getTeamType().toString()));
            mActingCaptain = number;
        }
    }

    public boolean hasActingCaptainOnCourt() {
        return mActingCaptain > 0;
    }

    public boolean isActingCaptain(int number) {
        return number == mActingCaptain;
    }

    public Set<Integer> getPossibleActingCaptains() {
        Set<Integer> players = new TreeSet<>();

        if (mActingCaptain > 0) {
            players.add(mActingCaptain);
        } else {
            for (int number : getPlayersOnCourt()) {
                if (!indoorTeamDefinition().isLibero(number)) {
                    players.add(number);
                }
            }
            if (hasWaitingMiddleBlocker()) {
                players.add(mWaitingMiddleBlocker);
            }
        }

        return players;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof IndoorTeamComposition) {
            IndoorTeamComposition other = (IndoorTeamComposition) obj;
            result = super.equals(other)
                    && (this.getPlayerAtPosition(PositionType.POSITION_1) == (other.getPlayerAtPosition(PositionType.POSITION_1)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_2) == (other.getPlayerAtPosition(PositionType.POSITION_2)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_3) == (other.getPlayerAtPosition(PositionType.POSITION_3)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_4) == (other.getPlayerAtPosition(PositionType.POSITION_4)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_5) == (other.getPlayerAtPosition(PositionType.POSITION_5)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_6) == (other.getPlayerAtPosition(PositionType.POSITION_6)))
                    && (this.isStartingLineupConfirmed() == other.isStartingLineupConfirmed())
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_1) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_1)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_2) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_2)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_3) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_3)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_4) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_4)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_5) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_5)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_6) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_6)))
                    && (this.canSubstitute() == other.canSubstitute())
                    && (this.getSubstitutions().equals(other.getSubstitutions()))
                    && (this.hasActingLibero() == other.hasActingLibero())
                    && (this.hasWaitingMiddleBlocker() == other.hasWaitingMiddleBlocker())
                    && (this.getActingCaptain() == other.getActingCaptain())
                    && (this.hasLiberoOnCourt() == other.hasLiberoOnCourt());
        }

        return result;
    }
}
