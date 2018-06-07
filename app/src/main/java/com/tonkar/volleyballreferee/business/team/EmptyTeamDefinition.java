package com.tonkar.volleyballreferee.business.team;

import android.graphics.Color;

import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.HashSet;
import java.util.Set;

public class EmptyTeamDefinition extends TeamDefinition {

    public EmptyTeamDefinition(final TeamType teamType) {
        super(GameType.TIME, teamType);
    }

    // For GSON Deserialization
    public EmptyTeamDefinition() {
        this(TeamType.HOME);
    }

    @Override
    public int getLiberoColor() {
        return Color.parseColor(TeamDefinition.DEFAULT_COLOR);
    }

    @Override
    public void setLiberoColor(int color) {}

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
    public Set<Integer> getLiberos() {
        return new HashSet<>();
    }

    @Override
    public void setCaptain(int number) {}

    @Override
    public int getCaptain() {
        return 0;
    }

    @Override
    public boolean isCaptain(int number) {
        return false;
    }

    @Override
    public Set<Integer> getPossibleCaptains() {
        return new HashSet<>();
    }
}
