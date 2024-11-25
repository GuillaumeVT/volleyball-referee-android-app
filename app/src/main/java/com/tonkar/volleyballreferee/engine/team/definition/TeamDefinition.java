package com.tonkar.volleyballreferee.engine.team.definition;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.*;

public abstract class TeamDefinition extends TeamDto {

    @SerializedName("classType")
    private       String   mClassType;
    @SerializedName("team")
    private final TeamType mTeamType;

    public TeamDefinition(GameType kind, String id, String createdBy, TeamType teamType) {
        super();
        mClassType = getClass().getName();
        mTeamType = teamType;
        setKind(kind);
        setId(id);
        setCreatedBy(createdBy);
        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        setCreatedAt(utcTime);
        setUpdatedAt(utcTime);
        setName("");
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    public int getNumberOfPlayers() {
        return getPlayers().size();
    }

    public void addPlayer(final int number) {
        if (!hasPlayer(number)) {
            Log.i(Tags.TEAM, String.format("Add player #%d to %s team", number, mTeamType));
            getPlayers().add(new PlayerDto(number));
        }
    }

    public void removePlayer(final int number) {
        if (hasPlayer(number)) {
            Log.i(Tags.TEAM, String.format("Remove player #%d from %s team", number, mTeamType));
            getPlayers().remove(new PlayerDto(number));
        }
    }

    PlayerDto getPlayer(final int number) {
        PlayerDto matchingPlayer = null;

        for (PlayerDto player : getPlayers()) {
            if (number == player.getNum()) {
                matchingPlayer = player;
                break;
            }
        }

        return matchingPlayer;
    }

    public boolean hasPlayer(final int number) {
        return getPlayers().contains(new PlayerDto(number));
    }

    public String getPlayerName(final int number) {
        String playerName = "";

        for (PlayerDto player : getPlayers()) {
            if (player.getNum() == number) {
                playerName = player.getName();
                break;
            }
        }

        return playerName;
    }

    public void setPlayerName(final int number, final String name) {
        Log.i(Tags.TEAM, String.format("Set name of player #%d to %s team as %s", number, mTeamType, name));
        for (PlayerDto player : getPlayers()) {
            if (player.getNum() == number) {
                player.setName(name);
                break;
            }
        }
    }

    public abstract int getExpectedNumberOfPlayersOnCourt();

    public abstract boolean isLibero(int number);

    public abstract boolean canAddLibero();

    public abstract void addLibero(int number);

    public abstract void removeLibero(int number);

    public abstract boolean isCaptain(int number);

    public abstract Set<Integer> getPossibleCaptains();

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof TeamDefinition other) {
            result = super.equals(other) && mClassType.equals(other.mClassType) && mTeamType.equals(other.mTeamType);
        }

        return result;
    }

}
