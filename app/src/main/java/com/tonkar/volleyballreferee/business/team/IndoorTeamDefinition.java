package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.Set;
import java.util.TreeSet;

public class IndoorTeamDefinition extends TeamDefinition {

    private       int          mLiberoColor;
    private final Set<Integer> mLiberos;

    public IndoorTeamDefinition(final TeamType teamType) {
        super(teamType);

        mLiberoColor = Integer.MIN_VALUE;
        mLiberos = new TreeSet<>();
    }

    public int getLiberoColor() {
        return mLiberoColor;
    }

    public void setLiberoColor(int color) {
        mLiberoColor = color;
    }

    public boolean isLibero(int number) {
        return mLiberos.contains(number);
    }

    public boolean canAddLibero() {
        int numberOfPlayers = getNumberOfPlayers();
        int numberOfLiberos = mLiberos.size();
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

    public void addLibero(final int number) {
        if (canAddLibero() && hasPlayer(number)) {
            Log.i("VBR-Team", String.format("Add player #%d as libero of %s team", number, getTeamType().toString()));
            mLiberos.add(number);
        }
    }

    public void removeLibero(final int number) {
        if (hasPlayer(number) && isLibero(number)) {
            Log.i("VBR-Team", String.format("Remove player #%d as libero from %s team", number, getTeamType().toString()));
            mLiberos.remove(number);
        }
    }

    Set<Integer> getLiberos() {
        return new TreeSet<>(mLiberos);
    }
}
