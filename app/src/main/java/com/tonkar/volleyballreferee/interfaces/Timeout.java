package com.tonkar.volleyballreferee.interfaces;

import com.google.gson.annotations.SerializedName;

public class Timeout {

    @SerializedName("hPoints")
    private final int mHomeTeamPoints;
    @SerializedName("gPoints")
    private final int mGuestTeamPoints;

    public Timeout() {
        this(0, 0);
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
