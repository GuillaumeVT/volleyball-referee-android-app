package com.tonkar.volleyballreferee.interfaces.team;

import com.google.gson.annotations.SerializedName;

public class Substitution {

    @SerializedName("pIn")
    private final int mPlayerIn;
    @SerializedName("pOut")
    private final int mPlayerOut;
    @SerializedName("hPoints")
    private final int mHomeTeamPoints;
    @SerializedName("gPoints")
    private final int mGuestTeamPoints;

    public Substitution() {
        this(0, 0, 0, 0);
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
