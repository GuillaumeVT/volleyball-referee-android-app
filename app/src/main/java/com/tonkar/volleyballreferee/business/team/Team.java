package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Team implements Serializable {

    private       String               mName;
    private final TeamType             mTeamType;
    private       int                  mColorId;
    private final Map<Integer, Player> mPlayers;

    Team(final TeamType teamType) {
        mName = "";
        mTeamType = teamType;
        mColorId = R.color.colorShirt1;
        mPlayers = new LinkedHashMap<>();
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
        Log.i("VBR-Team", String.format("%s team name is %s", mTeamType.toString(), mName));
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    public List<Integer> getPlayersOnCourt() {
        List<Integer> playersOnCourt = new ArrayList<>();

        for (Player player : mPlayers.values()) {
            if (!PositionType.BENCH.equals(player.getPosition())) {
                playersOnCourt.add(player.getNumber());
            }
        }

        Collections.sort(playersOnCourt, new Comparator<Integer>() {
            @Override
            public int compare(Integer n1, Integer n2) {
                return n1.compareTo(n2);
            }
        });

        Log.i("VBR-Team", String.format("Players on court for %s team: %s", mTeamType.toString(), playersOnCourt.toString()));

        return playersOnCourt;
    }

    public List<Integer> getPlayersOnBench() {
        List<Integer> playersOnBench = new ArrayList<>();

        for (Player player : mPlayers.values()) {
            if (PositionType.BENCH.equals(player.getPosition())) {
                playersOnBench.add(player.getNumber());
            }
        }

        Collections.sort(playersOnBench, new Comparator<Integer>() {
            @Override
            public int compare(Integer n1, Integer n2) {
                return n1.compareTo(n2);
            }
        });

        Log.i("VBR-Team", String.format("Players on bench for %s team: %s", mTeamType.toString(), playersOnBench.toString()));

        return playersOnBench;
    }

    public int getNumberOfPlayers() {
        return mPlayers.size();
    }

    protected abstract Player createPlayer(int number);

    public void addPlayer(final int number) {
        Log.i("VBR-Team", String.format("Add player #%d to %s team", number, mTeamType.toString()));
        mPlayers.put(number, createPlayer(number));
    }

    public void removePlayer(final int number) {
        Log.i("VBR-Team", String.format("Remove player #%d from %s team", number, mTeamType.toString()));
        mPlayers.remove(number);
    }

    public boolean hasPlayer(final int number) {
        return mPlayers.containsKey(number);
    }

    public PositionType getPlayerPosition(final int number) {
        PositionType positionType = null;

        if (hasPlayer(number)) {
            positionType = mPlayers.get(number).getPosition();
        }

        return positionType;
    }

    public boolean substitutePlayer(final int number, final PositionType positionType) {
        boolean result = false;

        int currentNumber = getPlayerAtPosition(positionType);

        Log.i("VBR-Team", String.format("Substitute player #%d of %s team by player #%d on position %s", currentNumber, mTeamType.toString(), number, positionType.toString()));

        if (hasPlayer(number)) {
            mPlayers.get(number).setPosition(positionType);
            result = true;
        }

        if (result && currentNumber > 0 && hasPlayer(currentNumber)) {
            mPlayers.get(currentNumber).setPosition(PositionType.BENCH);
            Log.i("VBR-Team", String.format("Player #%d of %s team is now on bench", currentNumber, mTeamType.toString()));
        }

        return result;
    }

    private int getPlayerAtPosition(final PositionType positionType) {
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

    public int getColorId() {
        return mColorId;
    }

    public void setColorId(int colorId) {
        mColorId = colorId;
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
