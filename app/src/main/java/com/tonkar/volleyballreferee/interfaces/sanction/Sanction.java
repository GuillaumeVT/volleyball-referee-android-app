package com.tonkar.volleyballreferee.interfaces.sanction;

import com.google.gson.annotations.SerializedName;

public class Sanction {

    @SerializedName("num")
    private final int          mPlayer;
    @SerializedName("card")
    private final SanctionType mSanctionType;
    @SerializedName("set")
    private final int          mSetIndex;
    @SerializedName("hPoints")
    private final int          mHomeTeamPoints;
    @SerializedName("gPoints")
    private final int          mGuestTeamPoints;

    public Sanction() {
        this(0, SanctionType.YELLOW, 0, 0, 0);
    }

    public Sanction(int player, SanctionType sanctionType, int setIndex, int homeTeamPoints, int guestTeamPoints) {
        mPlayer = player;
        mSanctionType = sanctionType;
        mSetIndex = setIndex;
        mHomeTeamPoints = homeTeamPoints;
        mGuestTeamPoints = guestTeamPoints;
    }

    public int getPlayer() {
        return mPlayer;
    }

    public SanctionType getSanctionType() {
        return mSanctionType;
    }

    public int getSetIndex() {
        return mSetIndex;
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
        } else if (obj instanceof Sanction) {
            Sanction other = (Sanction) obj;
            result = (this.getPlayer() == other.getPlayer())
                    && (this.getSanctionType().equals(other.getSanctionType()))
                    && (this.getSetIndex() == other.getSetIndex())
                    && (this.getHomeTeamPoints() == other.getHomeTeamPoints())
                    && (this.getGuestTeamPoints() == other.getGuestTeamPoints());
        }

        return result;
    }
}
