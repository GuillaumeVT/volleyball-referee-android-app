package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.Set;
import java.util.TreeSet;

public class IndoorTeamDefinition extends TeamDefinition {

    private       int          mLiberoColor;
    private final Set<Integer> mLiberos;
    private       int          mCaptain;

    public IndoorTeamDefinition(final TeamType teamType) {
        super(teamType);

        mLiberoColor = Integer.MIN_VALUE;
        mLiberos = new TreeSet<>();
        mCaptain = -1;
    }

    @Override
    public void removePlayer(final int number) {
        if (isLibero(number)) {
            removeLibero(number);
        }
        super.removePlayer(number);
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

    public Set<Integer> getLiberos() {
        return new TreeSet<>(mLiberos);
    }

    public void setCaptain(int number) {
        if (hasPlayer(number)) {
            Log.i("VBR-Team", String.format("Set player #%d as captain of %s team", number, getTeamType().toString()));
            mCaptain = number;
        }
    }

    public int getCaptain() {
        return mCaptain;
    }

    public boolean isCaptain(int number) {
        return number == mCaptain;
    }

    public Set<Integer> getPossibleCaptains() {
        Set<Integer> possibleCaptains = new TreeSet<>();

        for (int number : getPlayers()) {
            if (!isLibero(number)) {
                possibleCaptains.add(number);
            }
        }

        return possibleCaptains;
    }

}
