package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.tonkar.volleyballreferee.api.ApiPlayer;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.Set;
import java.util.TreeSet;

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
            Log.i(Tags.TEAM, String.format("Set player #%d as captain of %s team", number, getTeamType().toString()));
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

        for (ApiPlayer player : getPlayers()) {
            possibleCaptains.add(player.getNum());
        }

        return possibleCaptains;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof BeachTeamDefinition) {
            BeachTeamDefinition other = (BeachTeamDefinition) obj;
            result = super.equals(other);
        }

        return result;
    }
}
