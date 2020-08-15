package com.tonkar.volleyballreferee.engine.team.composition;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiCourt;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPlayer;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSubstitution;
import com.tonkar.volleyballreferee.engine.team.definition.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.IndoorPlayer;
import com.tonkar.volleyballreferee.engine.team.player.Player;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.engine.team.substitution.AlternativeSubstitutionsLimitation1;
import com.tonkar.volleyballreferee.engine.team.substitution.AlternativeSubstitutionsLimitation2;
import com.tonkar.volleyballreferee.engine.team.substitution.FivbSubstitutionsLimitation;
import com.tonkar.volleyballreferee.engine.team.substitution.NoSubstitutionsLimitation;
import com.tonkar.volleyballreferee.engine.team.substitution.SubstitutionsLimitation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IndoorTeamComposition extends TeamComposition {

    @SerializedName("startingLineupConfirmed")
    private       boolean                 mStartingLineupConfirmed;
    @SerializedName("startingLineup")
    private final ApiCourt                mStartingLineup;
    @SerializedName("substitutionsLimitation")
    private       SubstitutionsLimitation mSubstitutionsLimitation;
    @SerializedName("maxSubstitutionsPerSet")
    private final int                     mMaxSubstitutionsPerSet;
    @SerializedName("substitutions")
    private final List<ApiSubstitution>   mSubstitutions;
    @SerializedName("actingLibero")
    private       int                     mActingLibero;
    @SerializedName("middleBlockers")
    private final Set<Integer>            mMiddleBlockers;
    @SerializedName("waitingMiddleBlocker")
    private       int                     mWaitingMiddleBlocker;
    @SerializedName("secondaryCaptain")
    private       int                     mSecondaryCaptain;

    public IndoorTeamComposition(final TeamDefinition teamDefinition, int substitutionType, int maxSubstitutionsPerSet) {
        super(teamDefinition);

        mStartingLineupConfirmed = false;
        mStartingLineup = new ApiCourt();
        mMaxSubstitutionsPerSet = maxSubstitutionsPerSet;
        mSubstitutions = new ArrayList<>();
        mActingLibero = -1;
        mMiddleBlockers = new HashSet<>();
        mWaitingMiddleBlocker = -1;
        mSecondaryCaptain = -1;

        switch (substitutionType) {
            case Rules.FIVB_LIMITATION:
                mSubstitutionsLimitation = new FivbSubstitutionsLimitation();
                break;
            case Rules.ALTERNATIVE_LIMITATION_1:
                mSubstitutionsLimitation = new AlternativeSubstitutionsLimitation1();
                break;
            case Rules.ALTERNATIVE_LIMITATION_2:
                mSubstitutionsLimitation = new AlternativeSubstitutionsLimitation2();
                break;
            case Rules.NO_LIMITATION:
                mSubstitutionsLimitation = new NoSubstitutionsLimitation();
                break;
        }
    }

    // For GSON Deserialization
    public IndoorTeamComposition() {
        this(new IndoorTeamDefinition(), Rules.FIVB_LIMITATION, 0);
    }

    @Override
    protected Player createPlayer(int number) {
        return new IndoorPlayer(number);
    }

    @Override
    public boolean substitutePlayer(final int number, final PositionType positionType, int homeTeamPoints, int guestTeamPoints) {
        boolean result = false;

        if (isPossibleSubstitution(number, positionType)) {
            result = super.substitutePlayer(number, positionType, homeTeamPoints, guestTeamPoints);
        }

        return result;
    }

    public boolean undoSubstitution(ApiSubstitution substitution) {
        boolean result = false;

        if (isStartingLineupConfirmed()) {
            PositionType positionType = getPlayerPosition(substitution.getPlayerIn());

            if (!PositionType.BENCH.equals(positionType) && PositionType.BENCH.equals(getPlayerPosition(substitution.getPlayerOut()))) {
                for (Iterator<ApiSubstitution> iterator = mSubstitutions.iterator(); iterator.hasNext(); ) {
                    ApiSubstitution tmpSubstitution = iterator.next();
                    if (tmpSubstitution.equals(substitution)) {
                        iterator.remove();
                        mStartingLineupConfirmed = false;
                        substitutePlayer(substitution.getPlayerOut(), positionType, substitution.getHomePoints(), substitution.getGuestPoints());
                        mStartingLineupConfirmed = true;
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected void onSubstitution(int oldNumber, int newNumber, PositionType positionType, int homeTeamPoints, int guestTeamPoints) {
        Log.i(Tags.TEAM, String.format("Replacing player #%d by #%d for position %s of %s team", oldNumber, newNumber, positionType.toString(), getTeamDefinition().getTeamType().toString()));

        if (isStartingLineupConfirmed()) {
            if (getTeamDefinition().isLibero(newNumber)) {
                Log.i(Tags.TEAM, String.format("Player #%d of %s team is a libero and becomes acting libero", newNumber, getTeamDefinition().getTeamType().toString()));
                mActingLibero = newNumber;

                if (!getTeamDefinition().isLibero(oldNumber)) {
                    Log.i(Tags.TEAM, String.format("Player #%d of %s team is a middle blocker and is waiting outside", oldNumber, getTeamDefinition().getTeamType().toString()));
                    mMiddleBlockers.clear();
                    mWaitingMiddleBlocker = oldNumber;
                    mMiddleBlockers.add(oldNumber);
                    mMiddleBlockers.add(getPlayerAtPosition(positionType.oppositePosition()));
                }
            } else if (isMiddleBlocker(newNumber) && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(oldNumber)) {
                Log.i(Tags.TEAM, String.format("Player #%d of %s team is a middle blocker and is back on court", newNumber, getTeamDefinition().getTeamType().toString()));
                mWaitingMiddleBlocker = -1;
            } else {
                Log.i(Tags.TEAM, "Actual substitution");
                mSubstitutions.add(new ApiSubstitution(newNumber, oldNumber, homeTeamPoints, guestTeamPoints));

                if (isMiddleBlocker(oldNumber)) {
                    Log.i(Tags.TEAM, String.format("Player #%d of %s team is a new middle blocker", newNumber, getTeamDefinition().getTeamType().toString()));
                    mMiddleBlockers.remove(oldNumber);
                    mMiddleBlockers.add(newNumber);
                }
            }
        }
    }

    public void confirmStartingLineup() {
        mStartingLineupConfirmed = true;
        for (PositionType position : PositionType.listPositions(getTeamDefinition().getKind())) {
            mStartingLineup.setPlayerAt(getPlayerAtPosition(position), position);
        }
    }

    public boolean isStartingLineupConfirmed() {
        return mStartingLineupConfirmed;
    }

    public boolean canSubstitute() {
        return countRemainingSubstitutions() > 0;
    }

    public int countRemainingSubstitutions() {
        return mMaxSubstitutionsPerSet - mSubstitutions.size();
    }

    public Set<Integer> getPossibleSubstitutions(PositionType positionType) {
        Set<Integer> availablePlayers = new TreeSet<>(getPossibleSubstitutionsNoMax(positionType));
        Log.i(Tags.TEAM, String.format("Possible substitutions for position %s of %s team are %s", positionType.toString(), getTeamDefinition().getTeamType().toString(), availablePlayers.toString()));
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
                // Can only do a fixed number of substitutions
                if (canSubstitute()) {
                    // A player who was replaced can only do one return trip with his regular substitute player
                    if (isInvolvedInPastSubstitution(number)) {
                        if (canSubstitute(number)) {
                            availablePlayers.addAll(getSubstitutePlayers(number));
                        }
                    } else {
                        availablePlayers.addAll(getFreePlayersOnBench());
                        // The waiting middle blocker may be on the bench, but it should never be available
                        if (hasWaitingMiddleBlocker() && !isInvolvedInPastSubstitution(mWaitingMiddleBlocker)) {
                            availablePlayers.remove(mWaitingMiddleBlocker);
                        }
                    }
                }
                // If no libero is on the court, they can replace the player if he is at the back
                if (!hasLiberoOnCourt() && positionType.isAtTheBack()) {
                    for (ApiPlayer player : getTeamDefinition().getLiberos()) {
                        availablePlayers.add(player.getNum());
                    }
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
        return getTeamDefinition().isLibero(number) || getTeamDefinition().isLibero(getPlayerAtPosition(positionType));
    }

    private boolean isInvolvedInPastSubstitution(int number) {
        return mSubstitutionsLimitation.isInvolvedInPastSubstitution(mSubstitutions, number);
    }

    private boolean canSubstitute(int number) {
        return mSubstitutionsLimitation.canSubstitute(mSubstitutions, number);
    }

    private Set<Integer> getSubstitutePlayers(int number) {
        return mSubstitutionsLimitation.getSubstitutePlayers(mSubstitutions, number, getFreePlayersOnBench());
    }

    private List<Integer> getFreePlayersOnBench() {
        List<Integer> players = new ArrayList<>();

        for (ApiPlayer player : getTeamDefinition().getPlayers()) {
            if (PositionType.BENCH.equals(getPlayerPosition(player.getNum())) && !isInvolvedInPastSubstitution(player.getNum()) && !getTeamDefinition().isLibero(player.getNum())) {
                players.add(player.getNum());
            }
        }

        return players;
    }

    private boolean hasLiberoOnCourt() {
        boolean result = false;

        for (ApiPlayer player : getTeamDefinition().getLiberos()) {
            if (!PositionType.BENCH.equals(getPlayerPosition(player.getNum()))) {
                result = true;
            }
        }

        return result;
    }

    private boolean hasActingLibero() {
        return mActingLibero > -1;
    }

    private boolean hasSecondLibero() {
        return getTeamDefinition().getLiberos().size() > 1;
    }

    private int getSecondLibero() {
        int secondLibero = -1;

        for (ApiPlayer player : getTeamDefinition().getLiberos()) {
            if (player.getNum() != mActingLibero) {
                secondLibero = player.getNum();
            }
        }

        return secondLibero;
    }

    private boolean hasWaitingMiddleBlocker() {
        return mWaitingMiddleBlocker > -1;
    }

    private boolean isMiddleBlocker(int number) {
        return mMiddleBlockers.contains(number);
    }

    @Override
    public void rotateToNextPositions() {
        super.rotateToNextPositions();

        if (hasLiberoOnCourt() && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(getPlayerAtPosition(PositionType.POSITION_4))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_4);
        }
    }

    @Override
    public void rotateToPreviousPositions() {
        super.rotateToPreviousPositions();

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(getPlayerAtPosition(PositionType.POSITION_2))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_2);
        }
        if (hasActingLibero() && !hasLiberoOnCourt() && !hasWaitingMiddleBlocker() && isMiddleBlocker(getPlayerAtPosition(PositionType.POSITION_5))) {
            substitutePlayer(mActingLibero, PositionType.POSITION_5);
        }
    }

    public int checkPosition1Offence() {
        int middleBlockerNumber = -1;

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(getPlayerAtPosition(PositionType.POSITION_1))) {
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

    public List<ApiSubstitution> getSubstitutions() {
        return new ArrayList<>(mSubstitutions);
    }

    public ApiCourt getStartingLineup() {
        return mStartingLineup;
    }

    public PositionType getPlayerPositionInStartingLineup(int number) {
        return mStartingLineup.getPositionOf(number);
    }

    public int getPlayerAtPositionInStartingLineup(PositionType positionType) {
        return mStartingLineup.getPlayerAt(positionType);
    }

    public boolean hasGameCaptainOnCourt() {
        return isStartingLineupConfirmed() && (hasCaptainOnCourt() || hasSecondaryCaptainOnCourt());
    }

    public boolean isGameCaptain(int number) {
        return number == getGameCaptain() && ApiSanction.isPlayer(number);
    }

    public int getGameCaptain() {
        if (hasCaptainOnCourt()) {
            return getTeamDefinition().getCaptain();
        } else if (hasSecondaryCaptainOnCourt()) {
            return getSecondaryCaptain();
        } else {
            return -1;
        }
    }

    public void setGameCaptain(int number) {
        if (isStartingLineupConfirmed()
                && getTeamDefinition().hasPlayer(number)
                && !getTeamDefinition().isLibero(number)
                && !getTeamDefinition().isCaptain(number)
                && !isSecondaryCaptain(number)
                && !PositionType.BENCH.equals(getPlayerPosition(number))) {
            Log.i(Tags.TEAM, String.format("Player #%d of %s team is now secondary captain", number, getTeamDefinition().getTeamType().toString()));
            mSecondaryCaptain = number;
        }
    }

    private int getSecondaryCaptain() {
        return mSecondaryCaptain;
    }

    private boolean hasSecondaryCaptainOnCourt() {
        return mSecondaryCaptain > -1 && !PositionType.BENCH.equals(getPlayerPosition(mSecondaryCaptain));
    }

    private boolean isSecondaryCaptain(int number) {
        return number == mSecondaryCaptain;
    }

    public Set<Integer> getPossibleSecondaryCaptains() {
        Set<Integer> players = new TreeSet<>();

        for (int number : getPlayersOnCourt()) {
            if (!getTeamDefinition().isLibero(number) && !getTeamDefinition().isCaptain(number) && !isSecondaryCaptain(number)) {
                players.add(number);
            }
        }

        return players;
    }

    public int getWaitingMiddleBlocker() {
        return mWaitingMiddleBlocker;
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
                    && (this.getSecondaryCaptain() == other.getSecondaryCaptain())
                    && (this.hasLiberoOnCourt() == other.hasLiberoOnCourt());
        }

        return result;
    }

}
