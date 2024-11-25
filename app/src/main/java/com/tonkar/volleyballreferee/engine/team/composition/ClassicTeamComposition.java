package com.tonkar.volleyballreferee.engine.team.composition;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.engine.team.substitution.*;

import java.util.*;

public abstract class ClassicTeamComposition extends TeamComposition {

    @SerializedName("startingLineupConfirmed")
    private       boolean                 mStartingLineupConfirmed;
    @SerializedName("startingLineup")
    private final CourtDto                mStartingLineup;
    @SerializedName("substitutionsLimitation")
    private       SubstitutionsLimitation mSubstitutionsLimitation;
    @SerializedName("maxSubstitutionsPerSet")
    private final int                     mMaxSubstitutionsPerSet;
    @SerializedName("substitutions")
    private final List<SubstitutionDto>   mSubstitutions;
    @SerializedName("secondaryCaptain")
    private       int                     mSecondaryCaptain;

    ClassicTeamComposition(final TeamDefinition teamDefinition, int substitutionType, int maxSubstitutionsPerSet) {
        super(teamDefinition);

        mStartingLineupConfirmed = false;
        mStartingLineup = new CourtDto();
        mMaxSubstitutionsPerSet = maxSubstitutionsPerSet;
        mSubstitutions = new ArrayList<>();
        mSecondaryCaptain = -1;

        switch (substitutionType) {
            case Rules.FIVB_LIMITATION -> mSubstitutionsLimitation = new FivbSubstitutionsLimitation();
            case Rules.ALTERNATIVE_LIMITATION_1 -> mSubstitutionsLimitation = new AlternativeSubstitutionsLimitation1();
            case Rules.ALTERNATIVE_LIMITATION_2 -> mSubstitutionsLimitation = new AlternativeSubstitutionsLimitation2();
            case Rules.NO_LIMITATION -> mSubstitutionsLimitation = new NoSubstitutionsLimitation();
        }
    }

    public boolean isStartingLineupConfirmed() {
        return mStartingLineupConfirmed;
    }

    public void confirmStartingLineup() {
        mStartingLineupConfirmed = true;
        for (PositionType position : PositionType.listPositions(getTeamDefinition().getKind())) {
            mStartingLineup.setPlayerAt(getPlayerAtPosition(position), position);
        }
    }

    @Override
    public boolean substitutePlayer(final int number,
                                    final PositionType positionType,
                                    int homeTeamPoints,
                                    int guestTeamPoints,
                                    ActionOriginType actionOriginType) {
        boolean result = false;

        if (isPossibleSubstitution(number, positionType)) {
            result = super.substitutePlayer(number, positionType, homeTeamPoints, guestTeamPoints, actionOriginType);
        }

        return result;
    }

    public boolean undoSubstitution(SubstitutionDto substitution) {
        boolean result = false;

        if (isStartingLineupConfirmed()) {
            PositionType positionType = getPlayerPosition(substitution.getPlayerIn());

            if (!PositionType.BENCH.equals(positionType) && PositionType.BENCH.equals(getPlayerPosition(substitution.getPlayerOut()))) {
                for (Iterator<SubstitutionDto> iterator = mSubstitutions.iterator(); iterator.hasNext(); ) {
                    SubstitutionDto tmpSubstitution = iterator.next();
                    if (tmpSubstitution.equals(substitution)) {
                        iterator.remove();
                        mStartingLineupConfirmed = false;
                        substitutePlayer(substitution.getPlayerOut(), positionType, substitution.getHomePoints(),
                                         substitution.getGuestPoints(), ActionOriginType.USER);
                        mStartingLineupConfirmed = true;
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    public Set<Integer> getPossibleSubstitutions(PositionType positionType) {
        Set<Integer> availablePlayers = new TreeSet<>();

        // Once the starting line-up is confirmed, the rules must apply
        if (isStartingLineupConfirmed()) {
            // Can only do a fixed number of substitutions
            if (canSubstitute()) {
                int number = getPlayerAtPosition(positionType);

                // A player who was replaced can only do one return trip with his regular substitute player
                if (isInvolvedInPastSubstitution(number)) {
                    if (canSubstitute(number)) {
                        availablePlayers.addAll(getSubstitutePlayers(number));
                    }
                } else {
                    availablePlayers.addAll(getFreePlayersOnBench());
                }
            }
        } else {
            availablePlayers.addAll(getFreePlayersOnBench());
        }

        Log.i(Tags.TEAM,
              String.format("Possible substitutions for position %s of %s team are %s", positionType, getTeamDefinition().getTeamType(),
                            availablePlayers));

        return availablePlayers;
    }

    protected boolean isPossibleSubstitution(int number, PositionType positionType) {
        boolean result;

        if (PositionType.BENCH.equals(positionType) && !isStartingLineupConfirmed()) {
            result = true;
        } else {
            result = getPossibleSubstitutions(positionType).contains(number);
        }

        return result;
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
            Log.i(Tags.TEAM, "Actual substitution");
            mSubstitutions.add(new SubstitutionDto(newNumber, oldNumber, homeTeamPoints, guestTeamPoints));
        }
    }

    public boolean canSubstitute() {
        return countRemainingSubstitutions() > 0;
    }

    public int countRemainingSubstitutions() {
        return mMaxSubstitutionsPerSet - mSubstitutions.size();
    }

    protected boolean isInvolvedInPastSubstitution(int number) {
        return mSubstitutionsLimitation.isInvolvedInPastSubstitution(mSubstitutions, number);
    }

    protected boolean canSubstitute(int number) {
        return mSubstitutionsLimitation.canSubstitute(mSubstitutions, number);
    }

    protected Set<Integer> getSubstitutePlayers(int number) {
        return mSubstitutionsLimitation.getSubstitutePlayers(mSubstitutions, number, getFreePlayersOnBench());
    }

    protected List<Integer> getFreePlayersOnBench() {
        List<Integer> players = new ArrayList<>();

        for (PlayerDto player : getTeamDefinition().getPlayers()) {
            if (PositionType.BENCH.equals(getPlayerPosition(player.getNum())) && !isInvolvedInPastSubstitution(player.getNum())) {
                players.add(player.getNum());
            }
        }

        return players;
    }

    public List<SubstitutionDto> getSubstitutionsCopy() {
        return new ArrayList<>(mSubstitutions);
    }

    public CourtDto getStartingLineup() {
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
        return number == getGameCaptain() && SanctionDto.isPlayer(number);
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
        if (isStartingLineupConfirmed() && getTeamDefinition().hasPlayer(number) && !getTeamDefinition().isCaptain(
                number) && !isSecondaryCaptain(number) && !PositionType.BENCH.equals(getPlayerPosition(number))) {
            Log.i(Tags.TEAM, String.format("Player #%d of %s team is now secondary captain", number, getTeamDefinition().getTeamType()));
            mSecondaryCaptain = number;
        }
    }

    protected int getSecondaryCaptain() {
        return mSecondaryCaptain;
    }

    private boolean hasSecondaryCaptainOnCourt() {
        return mSecondaryCaptain > -1 && !PositionType.BENCH.equals(getPlayerPosition(mSecondaryCaptain));
    }

    protected boolean isSecondaryCaptain(int number) {
        return number == mSecondaryCaptain;
    }

    public Set<Integer> getPossibleSecondaryCaptains() {
        Set<Integer> players = new TreeSet<>();

        for (int number : getPlayersOnCourt()) {
            if (!getTeamDefinition().isCaptain(number) && !isSecondaryCaptain(number)) {
                players.add(number);
            }
        }

        return players;
    }
}
