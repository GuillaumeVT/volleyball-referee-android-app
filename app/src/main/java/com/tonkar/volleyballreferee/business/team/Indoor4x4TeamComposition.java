package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.api.ApiPlayer;
import com.tonkar.volleyballreferee.api.ApiSubstitution;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Indoor4x4TeamComposition extends TeamComposition {

    @SerializedName("startingLineupConfirmed")
    private       boolean               mStartingLineupConfirmed;
    @SerializedName("startingLineup")
    private final Map<Integer, Player>  mStartingLineup;
    @SerializedName("maxSubstitutionsPerSet")
    private final int                   mMaxSubstitutionsPerSet;
    @SerializedName("substitutions")
    private final Map<Integer, Integer> mSubstitutions;
    @SerializedName("fullSubstitutions")
    private final List<ApiSubstitution> mFullSubstitutions;
    @SerializedName("actingCaptain")
    private       int                   mActingCaptain;
    @SerializedName("forbiddenSub1")
    private final Set<Integer>          mForbiddenSub1;
    @SerializedName("forbiddenSub2")
    private final Set<Integer>          mForbiddenSub2;

    public Indoor4x4TeamComposition(final TeamDefinition teamDefinition, int maxSubstitutionsPerSet) {
        super(teamDefinition);

        mStartingLineupConfirmed = false;
        mStartingLineup = new LinkedHashMap<>();
        mMaxSubstitutionsPerSet = maxSubstitutionsPerSet;
        mSubstitutions = new LinkedHashMap<>();
        mFullSubstitutions = new ArrayList<>();
        mActingCaptain = -1;
        mForbiddenSub1 = new HashSet<>();
        mForbiddenSub2 = new HashSet<>();
    }

    // For GSON Deserialization
    public Indoor4x4TeamComposition() {
        this(new IndoorTeamDefinition(), 0);
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

    @Override
    protected void onSubstitution(int oldNumber, int newNumber, PositionType positionType, int homeTeamPoints, int guestTeamPoints) {
        Log.i(Tags.TEAM, String.format("Replacing player #%d by #%d for position %s of %s team", oldNumber, newNumber, positionType.toString(), indoorTeamDefinition().getTeamType().toString()));

        if (isStartingLineupConfirmed()) {
            Log.i(Tags.TEAM, "Actual substitution");
            mSubstitutions.put(newNumber, oldNumber);
            mFullSubstitutions.add(new ApiSubstitution(newNumber, oldNumber, homeTeamPoints, guestTeamPoints));

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

    public boolean canSubstitute() {
        return mSubstitutions.size() < mMaxSubstitutionsPerSet;
    }

    public Set<Integer> getPossibleSubstitutions(PositionType positionType) {
        Set<Integer> availablePlayers = new TreeSet<>();

        // Once the starting line-up is confirmed, the rules must apply
        if (isStartingLineupConfirmed()) {
            // Can only do a fix number of substitutions
            if (canSubstitute()) {
                // Substitutions are free
                availablePlayers.addAll(getPlayersOnBench());

                // A player who just served cannot serve again on the next rotation
                if (PositionType.POSITION_1.equals(positionType)) {
                    for (int player : mForbiddenSub1) {
                        availablePlayers.remove(player);
                    }
                }
                // A player who just served cannot serve again on the next rotation
                if (PositionType.POSITION_2.equals(positionType)) {
                    for (int player : mForbiddenSub2) {
                        availablePlayers.remove(player);
                    }
                }
            }
        } else {
            availablePlayers.addAll(getPlayersOnBench());
        }

        Log.i(Tags.TEAM, String.format("Possible substitutions for position %s of %s team are %s", positionType.toString(), indoorTeamDefinition().getTeamType().toString(), availablePlayers.toString()));

        return availablePlayers;
    }

    private boolean isPossibleSubstitution(int number, PositionType positionType) {
        boolean result;

        if (PositionType.POSITION_5.equals(positionType) || PositionType.POSITION_6.equals(positionType)) {
            result = false;
        } else if (PositionType.BENCH.equals(positionType) && !isStartingLineupConfirmed()) {
            result = true;
        } else {
            result = getPossibleSubstitutions(positionType).contains(number);
        }

        return result;
    }

    private List<Integer> getPlayersOnBench() {
        List<Integer> players = new ArrayList<>();

        for (ApiPlayer player : indoorTeamDefinition().getPlayers()) {
            if (PositionType.BENCH.equals(getPlayerPosition(player.getNum()))) {
                players.add(player.getNum());
            }
        }

        return players;
    }

    public List<ApiSubstitution> getSubstitutions() {
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
        if (isStartingLineupConfirmed() && indoorTeamDefinition().hasPlayer(number)) {
            Log.i(Tags.TEAM, String.format("Player #%d of %s team is now acting captain", number, indoorTeamDefinition().getTeamType().toString()));
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
            players.addAll(getPlayersOnCourt());
        }

        return players;
    }

    public void allowPosition1() {
        mForbiddenSub1.clear();
    }

    public void forbidPosition2ToCurrentServer() {
        mForbiddenSub2.add(getPlayerAtPosition(PositionType.POSITION_1));
    }

    @Override
    public void rotateToNextPositions() {
        super.rotateToNextPositions();

        if (mStartingLineupConfirmed) {
            mForbiddenSub1.addAll(mForbiddenSub2);
            mForbiddenSub2.clear();
            forbidPosition2ToCurrentServer();
        }
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
