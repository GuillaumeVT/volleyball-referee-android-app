package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.PositionType;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class TeamComposition implements Serializable {

    private final TeamDefinition       mTeamDefinition;
    private final Map<Integer, Player> mPlayers;

    TeamComposition(final TeamDefinition teamDefinition) {
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

        Log.i("VBR-Team", String.format("Players on court for %s team: %s", mTeamDefinition.getTeamType().toString(), playersOnCourt.toString()));

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

        Log.i("VBR-Team", String.format("Substitute player #%d of %s team by player #%d on position %s", oldNumber, mTeamDefinition.getTeamType().toString(), number, positionType.toString()));

        if (mTeamDefinition.hasPlayer(number)) {
            mPlayers.get(number).setPosition(positionType);
            result = true;
        }

        if (result && oldNumber > 0 && mTeamDefinition.hasPlayer(oldNumber)) {
            mPlayers.get(oldNumber).setPosition(PositionType.BENCH);
            Log.i("VBR-Team", String.format("Player #%d of %s team is now on bench", oldNumber, mTeamDefinition.getTeamType().toString()));
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

}
