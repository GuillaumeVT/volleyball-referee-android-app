package com.tonkar.volleyballreferee.engine.team.definition;

import android.util.Log;

import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.PlayerDto;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.*;

public class BeachTeamDefinition extends TeamDefinition {

    public BeachTeamDefinition(String id, String createdBy, TeamType teamType) {
        super(GameType.BEACH, id, createdBy, teamType);

        addPlayer(1);
        addPlayer(2);
        setCaptain(1);
    }

    // For GSON Deserialization
    public BeachTeamDefinition() {
        this("", "", TeamType.HOME);
    }

    @Override
    public boolean isLibero(int number) {
        return false;
    }

    @Override
    public boolean canAddLibero() {
        return false;
    }

    @Override
    public void addLibero(int number) {}

    @Override
    public void removeLibero(int number) {}

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
        return 2;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof BeachTeamDefinition other) {
            result = super.equals(other);
        }

        return result;
    }
}
