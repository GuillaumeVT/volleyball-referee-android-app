package com.tonkar.volleyballreferee.interfaces;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Timeout implements Serializable {

    @SerializedName("hPoints")
    private int mHomeTeamPoints;
    @SerializedName("gPoints")
    private int mGuestTeamPoints;

    public Timeout() {
        mHomeTeamPoints = 0;
        mGuestTeamPoints = 0;
    }

    public Timeout(int homeTeamPoints, int guestTeamPoints) {
        mHomeTeamPoints = homeTeamPoints;
        mGuestTeamPoints = guestTeamPoints;
    }

    public int getHomeTeamPoints() {
        return mHomeTeamPoints;
    }

    public int getGuestTeamPoints() {
        return mGuestTeamPoints;
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
        } else if (obj instanceof Timeout) {
            Timeout other = (Timeout) obj;
            result = (this.getHomeTeamPoints() == other.getHomeTeamPoints()) && (this.getGuestTeamPoints() == other.getGuestTeamPoints());
        }

        return result;
    }
}
