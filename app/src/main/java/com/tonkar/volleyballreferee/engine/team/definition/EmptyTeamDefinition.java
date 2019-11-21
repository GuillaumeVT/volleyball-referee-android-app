package com.tonkar.volleyballreferee.engine.team.definition;

import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.HashSet;
import java.util.Set;

public class EmptyTeamDefinition extends TeamDefinition {

    public EmptyTeamDefinition(String id, String createdBy, TeamType teamType) {
        super(GameType.TIME, id, createdBy, teamType);
    }

    // For GSON Deserialization
    public EmptyTeamDefinition() {
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
    public boolean isCaptain(int number) {
        return false;
    }

    @Override
    public Set<Integer> getPossibleCaptains() {
        return new HashSet<>();
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return 0;
    }
}
