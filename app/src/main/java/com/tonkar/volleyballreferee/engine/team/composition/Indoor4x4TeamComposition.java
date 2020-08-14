package com.tonkar.volleyballreferee.engine.team.composition;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiCourt;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPlayer;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSubstitution;
import com.tonkar.volleyballreferee.engine.team.definition.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.Indoor4x4Player;
import com.tonkar.volleyballreferee.engine.team.player.Player;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.engine.team.substitution.AlternativeSubstitutionsLimitation1;
import com.tonkar.volleyballreferee.engine.team.substitution.AlternativeSubstitutionsLimitation2;
import com.tonkar.volleyballreferee.engine.team.substitution.FivbSubstitutionsLimitation;
import com.tonkar.volleyballreferee.engine.team.substitution.NoSubstitutionsLimitation;
import com.tonkar.volleyballreferee.engine.team.substitution.SubstitutionsLimitation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Indoor4x4TeamComposition extends TeamComposition {

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
    @SerializedName("actingCaptain")
    private       int                     mActingCaptain;

    public Indoor4x4TeamComposition(final TeamDefinition teamDefinition, int substitutionType, int maxSubstitutionsPerSet) {
        super(teamDefinition);

        mStartingLineupConfirmed = false;
        mStartingLineup = new ApiCourt();
        mMaxSubstitutionsPerSet = maxSubstitutionsPerSet;
        mSubstitutions = new ArrayList<>();
        mActingCaptain = -1;

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
    public Indoor4x4TeamComposition() {
        this(new IndoorTeamDefinition(), Rules.NO_LIMITATION, 0);
    }

    @Override
    protected Player createPlayer(int number) {
        return new Indoor4x4Player(number);
    }

    private IndoorTeamDefinition indoorTeamDefinition() {
        return (IndoorTeamDefinition) getTeamDefinition();
    }

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
        Log.i(Tags.TEAM, String.format("Replacing player #%d by #%d for position %s of %s team", oldNumber, newNumber, positionType.toString(), indoorTeamDefinition().getTeamType().toString()));

        if (isStartingLineupConfirmed()) {
            Log.i(Tags.TEAM, "Actual substitution");
            mSubstitutions.add(new ApiSubstitution(newNumber, oldNumber, homeTeamPoints, guestTeamPoints));

            // A captain on the bench can no longer be acting captain
            if (indoorTeamDefinition().isCaptain(oldNumber) || isActingCaptain(oldNumber)) {
                Log.i(Tags.TEAM, String.format("Player #%d acting captain of %s team leaves the court", oldNumber, indoorTeamDefinition().getTeamType().toString()));
                mActingCaptain = -1;
            }

            // The game captain coming back on the court is automatically the captain again
            if (indoorTeamDefinition().isCaptain(newNumber)) {
                Log.i(Tags.TEAM, String.format("Player #%d captain of %s team is back on court", newNumber, indoorTeamDefinition().getTeamType().toString()));
                setActingCaptain(indoorTeamDefinition().getCaptain());
            }
        }
    }

    public void confirmStartingLineup() {
        mStartingLineupConfirmed = true;
        for (PositionType position : PositionType.listPositions(getTeamDefinition().getKind())) {
            mStartingLineup.setPlayerAt(getPlayerAtPosition(position), position);
        }

        if (!PositionType.BENCH.equals(getPlayerPosition(indoorTeamDefinition().getCaptain()))) {
            setActingCaptain(indoorTeamDefinition().getCaptain());
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

        Log.i(Tags.TEAM, String.format("Possible substitutions for position %s of %s team are %s", positionType.toString(), indoorTeamDefinition().getTeamType().toString(), availablePlayers.toString()));

        return availablePlayers;
    }

    private boolean isPossibleSubstitution(int number, PositionType positionType) {
        boolean result;

        if (PositionType.BENCH.equals(positionType) && !isStartingLineupConfirmed()) {
            result = true;
        } else {
            result = getPossibleSubstitutions(positionType).contains(number);
        }

        return result;
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

        for (ApiPlayer player : indoorTeamDefinition().getPlayers()) {
            if (PositionType.BENCH.equals(getPlayerPosition(player.getNum())) && !isInvolvedInPastSubstitution(player.getNum())) {
                players.add(player.getNum());
            }
        }

        return players;
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

    public int getActingCaptain() {
        return mActingCaptain;
    }

    public void setActingCaptain(int number) {
        if (isStartingLineupConfirmed() && indoorTeamDefinition().hasPlayer(number)) {
            Log.i(Tags.TEAM, String.format("Player #%d of %s team is now acting captain", number, indoorTeamDefinition().getTeamType().toString()));
            mActingCaptain = number;
        }
    }

    public boolean hasActingCaptainOnCourt() {
        return mActingCaptain > -1;
    }

    public boolean isActingCaptain(int number) {
        return number == mActingCaptain;
    }

    public Set<Integer> getPossibleActingCaptains() {
        Set<Integer> players = new TreeSet<>();

        if (mActingCaptain > -1) {
            players.add(mActingCaptain);
        } else {
            players.addAll(getPlayersOnCourt());
        }

        return players;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Indoor4x4TeamComposition) {
            Indoor4x4TeamComposition other = (Indoor4x4TeamComposition) obj;
            result = super.equals(other)
                    && (this.getPlayerAtPosition(PositionType.POSITION_1) == (other.getPlayerAtPosition(PositionType.POSITION_1)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_2) == (other.getPlayerAtPosition(PositionType.POSITION_2)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_3) == (other.getPlayerAtPosition(PositionType.POSITION_3)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_4) == (other.getPlayerAtPosition(PositionType.POSITION_4)))
                    && (this.isStartingLineupConfirmed() == other.isStartingLineupConfirmed())
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_1) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_1)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_2) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_2)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_3) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_3)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_4) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_4)))
                    && (this.canSubstitute() == other.canSubstitute())
                    && (this.getSubstitutions().equals(other.getSubstitutions()))
                    && (this.getActingCaptain() == other.getActingCaptain());
        }

        return result;
    }

}
