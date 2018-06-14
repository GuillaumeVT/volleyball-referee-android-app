package com.tonkar.volleyballreferee.business.team;

import android.graphics.Color;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.HashSet;
import java.util.Set;

public class BeachTeamDefinition extends TeamDefinition {

    @SerializedName("captain")
    private int mCaptain;

    public BeachTeamDefinition(final TeamType teamType) {
        super(GameType.BEACH, teamType);

        addPlayer(1);
        addPlayer(2);

        mCaptain = 1;
    }

    // For GSON Deserialization
    public BeachTeamDefinition() {
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
    public void setCaptain(int number) {
        if (hasPlayer(number)) {
            Log.i("VBR-Team", String.format("Set player #%d as captain of %s team", number, getTeamType().toString()));
            mCaptain = number;
        }
    }

    @Override
    public int getCaptain() {
        return mCaptain;
    }

    @Override
    public boolean isCaptain(int number) {
        return number == mCaptain;
    }

    @Override
    public Set<Integer> getPossibleCaptains() {
        return getPlayers();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof BeachTeamDefinition) {
            BeachTeamDefinition other = (BeachTeamDefinition) obj;
            result = super.equals(other) && (this.getCaptain() == other.getCaptain());
        }

        return result;
    }
}
