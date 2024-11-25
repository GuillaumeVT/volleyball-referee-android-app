package com.tonkar.volleyballreferee.engine.team.composition;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.PlayerDto;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.definition.*;
import com.tonkar.volleyballreferee.engine.team.player.*;

import java.util.*;

public class IndoorTeamComposition extends ClassicTeamComposition {

    @SerializedName("actingLibero")
    private       int          mActingLibero;
    @SerializedName("middleBlockers")
    private final Set<Integer> mMiddleBlockers;
    @SerializedName("waitingMiddleBlocker")
    private       int          mWaitingMiddleBlocker;

    public IndoorTeamComposition(final TeamDefinition teamDefinition, int substitutionType, int maxSubstitutionsPerSet) {
        super(teamDefinition, substitutionType, maxSubstitutionsPerSet);

        mActingLibero = -1;
        mMiddleBlockers = new HashSet<>();
        mWaitingMiddleBlocker = -1;
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
    protected void onSubstitution(int oldNumber,
                                  int newNumber,
                                  PositionType positionType,
                                  int homeTeamPoints,
                                  int guestTeamPoints,
                                  ActionOriginType actionOriginType) {
        Log.i(Tags.TEAM, String.format("Replacing player #%d by #%d for position %s of %s team", oldNumber, newNumber, positionType,
                                       getTeamDefinition().getTeamType()));

        if (isStartingLineupConfirmed()) {
            if (getTeamDefinition().isLibero(newNumber)) {
                Log.i(Tags.TEAM, String.format("Player #%d of %s team is a libero and becomes acting libero", newNumber,
                                               getTeamDefinition().getTeamType()));
                mActingLibero = newNumber;

                if (!getTeamDefinition().isLibero(oldNumber)) {
                    Log.i(Tags.TEAM, String.format("Player #%d of %s team is a middle blocker and is waiting outside", oldNumber,
                                                   getTeamDefinition().getTeamType()));

                    if (mMiddleBlockers.size() == 1) {
                        int middleBlocker = mMiddleBlockers.iterator().next();
                        if (middleBlocker == getPlayerAtPosition(positionType.oppositePosition())) {
                            // There is already a middle blocker and opposite to the player, player becomes middle blocker
                            addMiddleBlocker(oldNumber);
                        } else if (oldNumber == middleBlocker) {
                            middleBlockerOnBench(middleBlocker);
                        } else {
                            // There is already a middle blocker but not opposite to the player, reset and player and opposite player both become middle blockers
                            setMiddleBlockers(oldNumber, positionType);
                        }
                    } else {
                        // The player and opposite player both become middle blockers
                        setMiddleBlockers(oldNumber, positionType);
                    }
                }
            } else if (isMiddleBlocker(newNumber) && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(oldNumber)) {
                if (ActionOriginType.APPLICATION.equals(actionOriginType)) {
                    // The middle blocker is back on court as a result of a rotation
                    Log.i(Tags.TEAM, String.format("Player #%d of %s team is a middle blocker and is back on court", newNumber,
                                                   getTeamDefinition().getTeamType()));
                    middleBlockerOnCourt();
                } else {
                    // The middle blocker is back on court as a result of a substitution, the user wants this middle blocker to stay in defence
                    Log.i(Tags.TEAM, String.format("Player #%d of %s team is a middle blocker and will stay on court in defence", newNumber,
                                                   getTeamDefinition().getTeamType()));
                    removeMiddleBlocker(newNumber);
                }
            } else {
                Log.i(Tags.TEAM, "Actual substitution");
                super.onSubstitution(oldNumber, newNumber, positionType, homeTeamPoints, guestTeamPoints, actionOriginType);

                if (isMiddleBlocker(oldNumber)) {
                    Log.i(Tags.TEAM,
                          String.format("Player #%d of %s team is a new middle blocker", newNumber, getTeamDefinition().getTeamType()));
                    mMiddleBlockers.remove(oldNumber);
                    mMiddleBlockers.add(newNumber);
                }
            }
        }
    }

    @Override
    public Set<Integer> getPossibleSubstitutions(PositionType positionType) {
        Set<Integer> availablePlayers = new TreeSet<>(getPossibleSubstitutionsNoMax(positionType));
        Log.i(Tags.TEAM,
              String.format("Possible substitutions for position %s of %s team are %s", positionType, getTeamDefinition().getTeamType(),
                            availablePlayers));
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
                    for (PlayerDto player : getTeamDefinition().getLiberos()) {
                        availablePlayers.add(player.getNum());
                    }
                }
            }
        } else {
            availablePlayers.addAll(getFreePlayersOnBench());
        }

        return availablePlayers;
    }

    @Override
    protected boolean isPossibleSubstitution(int number, PositionType positionType) {
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

    @Override
    protected List<Integer> getFreePlayersOnBench() {
        List<Integer> players = new ArrayList<>();

        for (PlayerDto player : getTeamDefinition().getPlayers()) {
            if (PositionType.BENCH.equals(getPlayerPosition(player.getNum())) && !isInvolvedInPastSubstitution(
                    player.getNum()) && !getTeamDefinition().isLibero(player.getNum())) {
                players.add(player.getNum());
            }
        }

        return players;
    }

    private boolean hasLiberoOnCourt() {
        boolean result = false;

        for (PlayerDto player : getTeamDefinition().getLiberos()) {
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

        for (PlayerDto player : getTeamDefinition().getLiberos()) {
            if (player.getNum() != mActingLibero) {
                secondLibero = player.getNum();
            }
        }

        return secondLibero;
    }

    private boolean hasWaitingMiddleBlocker() {
        return mWaitingMiddleBlocker > -1;
    }

    public boolean isMiddleBlocker(int number) {
        return mMiddleBlockers.contains(number);
    }

    public int getWaitingMiddleBlocker() {
        return mWaitingMiddleBlocker;
    }

    private void addMiddleBlocker(int number) {
        mMiddleBlockers.add(number);
        middleBlockerOnBench(number);
    }

    private void middleBlockerOnBench(int number) {
        mWaitingMiddleBlocker = number;
    }

    private void middleBlockerOnCourt() {
        mWaitingMiddleBlocker = -1;
    }

    private void removeMiddleBlocker(int number) {
        middleBlockerOnCourt();
        mMiddleBlockers.remove(number);
    }

    private void setMiddleBlockers(int number, PositionType positionType) {
        mMiddleBlockers.clear();
        addMiddleBlocker(number);
        mMiddleBlockers.add(getPlayerAtPosition(positionType.oppositePosition()));
    }

    @Override
    public void rotateToNextPositions() {
        super.rotateToNextPositions();

        if (hasLiberoOnCourt() && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(getPlayerAtPosition(PositionType.POSITION_4))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_4, ActionOriginType.APPLICATION);
        }
    }

    @Override
    public void rotateToPreviousPositions() {
        super.rotateToPreviousPositions();

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(
                getPlayerAtPosition(PositionType.POSITION_2))) {
            substitutePlayer(mWaitingMiddleBlocker, PositionType.POSITION_2, ActionOriginType.APPLICATION);
        }
        if (hasActingLibero() && !hasLiberoOnCourt() && !hasWaitingMiddleBlocker() && isMiddleBlocker(
                getPlayerAtPosition(PositionType.POSITION_5))) {
            substitutePlayer(mActingLibero, PositionType.POSITION_5, ActionOriginType.APPLICATION);
        }
    }

    public int checkPosition1Offence() {
        int middleBlockerNumber = -1;

        if (hasActingLibero() && hasLiberoOnCourt() && hasWaitingMiddleBlocker() && getTeamDefinition().isLibero(
                getPlayerAtPosition(PositionType.POSITION_1))) {
            middleBlockerNumber = mWaitingMiddleBlocker;
        }

        return middleBlockerNumber;
    }

    public int checkPosition1Defence() {
        int liberoNumber = -1;

        if (hasActingLibero() && !hasLiberoOnCourt() && !hasWaitingMiddleBlocker() && isMiddleBlocker(
                getPlayerAtPosition(PositionType.POSITION_1))) {
            liberoNumber = mActingLibero;
        }

        return liberoNumber;
    }

    @Override
    public void setGameCaptain(int number) {
        super.setGameCaptain(number);
    }

    @Override
    public Set<Integer> getPossibleSecondaryCaptains() {
        Set<Integer> players = new TreeSet<>();

        for (int number : getPlayersOnCourt()) {
            if (!getTeamDefinition().isCaptain(number) && !isSecondaryCaptain(number)) {
                players.add(number);
            }
        }

        return players;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof IndoorTeamComposition other) {
            result = super.equals(other) && (this.getPlayerAtPosition(PositionType.POSITION_1) == (other.getPlayerAtPosition(
                    PositionType.POSITION_1))) && (this.getPlayerAtPosition(PositionType.POSITION_2) == (other.getPlayerAtPosition(
                    PositionType.POSITION_2))) && (this.getPlayerAtPosition(PositionType.POSITION_3) == (other.getPlayerAtPosition(
                    PositionType.POSITION_3))) && (this.getPlayerAtPosition(PositionType.POSITION_4) == (other.getPlayerAtPosition(
                    PositionType.POSITION_4))) && (this.getPlayerAtPosition(PositionType.POSITION_5) == (other.getPlayerAtPosition(
                    PositionType.POSITION_5))) && (this.getPlayerAtPosition(PositionType.POSITION_6) == (other.getPlayerAtPosition(
                    PositionType.POSITION_6))) && (this.isStartingLineupConfirmed() == other.isStartingLineupConfirmed()) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_1) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_1))) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_2) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_2))) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_3) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_3))) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_4) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_4))) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_5) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_5))) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_6) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_6))) && (this.canSubstitute() == other.canSubstitute()) && (this
                    .getSubstitutionsCopy()
                    .equals(other.getSubstitutionsCopy())) && (this.hasActingLibero() == other.hasActingLibero()) && (this.hasWaitingMiddleBlocker() == other.hasWaitingMiddleBlocker()) && (this.getSecondaryCaptain() == other.getSecondaryCaptain()) && (this.hasLiberoOnCourt() == other.hasLiberoOnCourt());
        }

        return result;
    }

}
