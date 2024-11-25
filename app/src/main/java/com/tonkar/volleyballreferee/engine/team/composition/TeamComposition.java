package com.tonkar.volleyballreferee.engine.team.composition;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.PlayerDto;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.*;

import java.util.*;

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

        for (PlayerDto player : mTeamDefinition.getPlayers()) {
            mPlayers.put(player.getNum(), createPlayer(player.getNum()));
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

        Log.i(Tags.TEAM, String.format("Players on court for %s team: %s", mTeamDefinition.getTeamType(), playersOnCourt));

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

    public boolean substitutePlayer(final int number, final PositionType positionType, ActionOriginType actionOriginType) {
        return substitutePlayer(number, positionType, 0, 0, actionOriginType);
    }

    public boolean substitutePlayer(final int number,
                                    final PositionType positionType,
                                    int homeTeamPoints,
                                    int guestTeamPoints,
                                    ActionOriginType actionOriginType) {
        boolean result = false;

        int oldNumber = getPlayerAtPosition(positionType);

        Log.i(Tags.TEAM,
              String.format("Substitute player #%d of %s team by player #%d on position %s", oldNumber, mTeamDefinition.getTeamType(),
                            number, positionType));

        if (mTeamDefinition.hasPlayer(number)) {
            mPlayers.get(number).setPosition(positionType);
            result = true;
        }

        if (result && oldNumber > -1 && mTeamDefinition.hasPlayer(oldNumber)) {
            mPlayers.get(oldNumber).setPosition(PositionType.BENCH);
            Log.i(Tags.TEAM, String.format("Player #%d of %s team is now on bench", oldNumber, mTeamDefinition.getTeamType()));
        }

        if (result) {
            onSubstitution(oldNumber, number, positionType, homeTeamPoints, guestTeamPoints, actionOriginType);
        }

        return result;
    }

    protected abstract void onSubstitution(int oldNumber,
                                           int newNumber,
                                           PositionType positionType,
                                           int homeTeamPoints,
                                           int guestTeamPoints,
                                           ActionOriginType actionOriginType);

    public int getPlayerAtPosition(final PositionType positionType) {
        int number = -1;

        if (!PositionType.BENCH.equals(positionType)) {
            for (Player player : mPlayers.values()) {
                if (player.getPosition().equals(positionType)) {
                    number = player.getNumber();
                    break;
                }
            }
        }

        return number;
    }

    public boolean hasCaptainOnCourt() {
        return !PositionType.BENCH.equals(getPlayerPosition(mTeamDefinition.getCaptain()));
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

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof TeamComposition other) {
            result = mClassType.equals(other.mClassType) && mTeamDefinition.equals(other.mTeamDefinition);
        }

        return result;
    }
}
