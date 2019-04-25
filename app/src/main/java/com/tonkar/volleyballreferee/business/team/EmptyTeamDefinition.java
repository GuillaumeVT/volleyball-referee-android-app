package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.HashSet;
import java.util.Set;

public class EmptyTeamDefinition extends TeamDefinition {

    public EmptyTeamDefinition(final String createdBy, final TeamType teamType) {
        super(createdBy, GameType.TIME, teamType);
    }

    // For GSON Deserialization
    public EmptyTeamDefinition() {
        this("", TeamType.HOME);
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
}
