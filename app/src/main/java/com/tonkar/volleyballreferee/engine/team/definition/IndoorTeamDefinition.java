package com.tonkar.volleyballreferee.engine.team.definition;

import android.util.Log;

import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.PlayerDto;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.*;

public class IndoorTeamDefinition extends TeamDefinition {

    public IndoorTeamDefinition(GameType kind, String id, String createdBy, TeamType teamType) {
        super(kind, id, createdBy, teamType);
    }

    // For GSON Deserialization
    public IndoorTeamDefinition() {
        this(GameType.INDOOR, "", "", TeamType.HOME);
    }

    @Override
    public void removePlayer(final int number) {
        if (isLibero(number)) {
            removeLibero(number);
        }
        if (isCaptain(number)) {
            super.setCaptain(-1);
        }
        super.removePlayer(number);
    }

    @Override
    public void setPlayerName(final int number, final String name) {
        super.setPlayerName(number, name);
        for (PlayerDto player : getLiberos()) {
            if (player.getNum() == number) {
                player.setName(name);
            }
        }
    }

    @Override
    public boolean isLibero(int number) {
        return getLiberos().contains(new PlayerDto(number));
    }

    @Override
    public boolean canAddLibero() {
        int numberOfPlayers = getNumberOfPlayers();
        int numberOfLiberos = getLiberos().size();
        boolean can;

        if (numberOfPlayers < 7) {
            can = false;
        } else if (numberOfPlayers < 8) {
            can = numberOfLiberos < 1;
        } else {
            can = numberOfLiberos < 2;
        }

        return can;
    }

    @Override
    public void addLibero(final int number) {
        if (canAddLibero() && hasPlayer(number)) {
            Log.i(Tags.TEAM, String.format("Add player #%d as libero of %s team", number, getTeamType()));
            PlayerDto player = getPlayer(number);
            getLiberos().add(player);
        }
    }

    @Override
    public void removeLibero(final int number) {
        if (hasPlayer(number) && isLibero(number)) {
            Log.i(Tags.TEAM, String.format("Remove player #%d as libero from %s team", number, getTeamType()));
            getLiberos().remove(new PlayerDto(number));
        }
    }

    @Override
    public void setCaptain(int number) {
        if (hasPlayer(number)) {
            Log.i(Tags.TEAM, String.format("Set player #%d as captain of %s team", number, getTeamType()));
            super.setCaptain(number);
        }
    }

    @Override
    public boolean isCaptain(int number) {
        return number == getCaptain();
    }

    @Override
    public Set<Integer> getPossibleCaptains() {
        Set<Integer> possibleCaptains = new TreeSet<>();

        for (PlayerDto player : getPlayers()) {
            possibleCaptains.add(player.getNum());
        }

        return possibleCaptains;
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return switch (getKind()) {
            case INDOOR -> 6;
            case INDOOR_4X4 -> 4;
            default -> 0;
        };
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof IndoorTeamDefinition other) {
            result = super.equals(other);
        }

        return result;
    }

}
