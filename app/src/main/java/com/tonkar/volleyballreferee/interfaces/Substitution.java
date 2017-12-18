package com.tonkar.volleyballreferee.interfaces;

import java.io.Serializable;

public class Substitution implements Serializable {

    private int mPlayerIn;
    private int mPlayerOut;
    private int mHomeTeamPoints;
    private int mGuestTeamPoints;

    public Substitution() {
        mPlayerIn = 0;
        mPlayerOut = 0;
        mHomeTeamPoints = 0;
        mGuestTeamPoints = 0;
    }

    public Substitution(int playerIn, int playerOut, int homeTeamPoints, int guestTeamPoints) {
        mPlayerIn = playerIn;
        mPlayerOut = playerOut;
        mHomeTeamPoints = homeTeamPoints;
        mGuestTeamPoints = guestTeamPoints;
    }

    public int getPlayerIn() {
        return mPlayerIn;
    }

    public int getPlayerOut() {
        return mPlayerOut;
    }

    public int getHomeTeamPoints() {
        return mHomeTeamPoints;
    }

    public int getGuestTeamPoints() {
        return mGuestTeamPoints;
    }

    public void setPlayerIn(int playerIn) {
        mPlayerIn = playerIn;
    }

    public void setPlayerOut(int playerOut) {
        mPlayerOut = playerOut;
    }

    public void setHomeTeamPoints(int homeTeamPoints) {
        mHomeTeamPoints = homeTeamPoints;
    }

    public void setGuestTeamPoints(int guestTeamPoints) {
        mGuestTeamPoints = guestTeamPoints;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Substitution) {
            Substitution other = (Substitution) obj;
            result = (this.getPlayerIn() == other.getPlayerIn())
                    && (this.getPlayerOut() == other.getPlayerOut())
                    && (this.getHomeTeamPoints() == other.getHomeTeamPoints())
                    && (this.getGuestTeamPoints() == other.getGuestTeamPoints());
        }

        return result;
    }
}
