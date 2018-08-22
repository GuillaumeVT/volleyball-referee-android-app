package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class TeamComposition {

    @SerializedName("classType")
    private       String               mClassType;
    @SerializedName("teamDef")
    private final TeamDefinition       mTeamDefinition;
    @SerializedName("players")
    private final Map<Integer, Player> mPlayers;

    TeamComposition(final TeamDefinition teamDefinition) {
        mClassType = getClass().getName();
        mTeamDefinition = teamDefinition;
        mPlayers = new LinkedHashMap<>();

        for (int number : mTeamDefinition.getPlayers()) {
            mPlayers.put(number, createPlayer(number));
        }
    }

    public TeamDefinition getTeamDefinition() {
        return mTeamDefinition;
    }

    public Set<Integer> getPlayersOnCourt() {
        Set<Integer> playersOnCourt = new TreeSet<>();

        for (Player player : mPlayers.values()) {
            if (!PositionType.BENCH.equals(player.getPosition())) {
                playersOnCourt.add(player.getNumber());
            }
        }

        Log.i(Tags.TEAM, String.format("Players on court for %s team: %s", mTeamDefinition.getTeamType().toString(), playersOnCourt.toString()));

        return playersOnCourt;
    }

    protected abstract Player createPlayer(int number);

    public PositionType getPlayerPosition(final int number) {
        PositionType positionType = null;

        if (mTeamDefinition.hasPlayer(number)) {
            positionType = mPlayers.get(number).getPosition();
        }

        return positionType;
    }

    public boolean substitutePlayer(final int number, final PositionType positionType) {
        return substitutePlayer(number, positionType, 0, 0);
    }

    public boolean substitutePlayer(final int number, final PositionType positionType, int homeTeamPoints, int guestTeamPoints) {
        boolean result = false;

        int oldNumber = getPlayerAtPosition(positionType);

        Log.i(Tags.TEAM, String.format("Substitute player #%d of %s team by player #%d on position %s", oldNumber, mTeamDefinition.getTeamType().toString(), number, positionType.toString()));

        if (mTeamDefinition.hasPlayer(number)) {
            mPlayers.get(number).setPosition(positionType);
            result = true;
        }

        if (result && oldNumber > 0 && mTeamDefinition.hasPlayer(oldNumber)) {
            mPlayers.get(oldNumber).setPosition(PositionType.BENCH);
            Log.i(Tags.TEAM, String.format("Player #%d of %s team is now on bench", oldNumber, mTeamDefinition.getTeamType().toString()));
        }

        if (result) {
            onSubstitution(oldNumber, number, positionType, homeTeamPoints, guestTeamPoints);
        }

        return result;
    }

    protected abstract void onSubstitution(int oldNumber, int newNumber, PositionType positionType, int homeTeamPoints, int guestTeamPoints);

    public int getPlayerAtPosition(final PositionType positionType) {
        int number = -1;

        if (!PositionType.BENCH.equals(positionType)) {
            for (Player player : mPlayers.values()) {
                if (player.getPosition().equals(positionType)) {
                    number = player.getNumber();
                }
            }
        }

        return number;
    }

    public void rotateToNextPositions() {
        for (Integer number : getPlayersOnCourt()) {
            final Player player = mPlayers.get(number);
            player.turnToNextPosition();
        }
    }

    public void rotateToPreviousPositions() {
        for (Integer number : getPlayersOnCourt()) {
            final Player player = mPlayers.get(number);
            player.turnToPreviousPosition();
        }
    }

    private String getClassType() {
        return mClassType;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof TeamComposition) {
            TeamComposition other = (TeamComposition) obj;
            result = (this.getClassType().equals(other.getClassType())) && (this.getTeamDefinition().equals(other.getTeamDefinition()));
        }

        return result;
    }

}
