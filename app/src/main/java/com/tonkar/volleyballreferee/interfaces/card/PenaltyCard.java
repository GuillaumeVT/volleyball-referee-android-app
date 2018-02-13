package com.tonkar.volleyballreferee.interfaces.card;

import com.google.gson.annotations.SerializedName;

public class PenaltyCard {

    @SerializedName("num")
    private final int             mPlayer;
    @SerializedName("card")
    private final PenaltyCardType mPenaltyCardType;
    @SerializedName("set")
    private final int             mSetIndex;
    @SerializedName("hPoints")
    private final int             mHomeTeamPoints;
    @SerializedName("gPoints")
    private final int             mGuestTeamPoints;

    public PenaltyCard() {
        this(0, PenaltyCardType.YELLOW, 0, 0, 0);
    }

    public PenaltyCard(int player, PenaltyCardType penaltyCardType, int setIndex, int homeTeamPoints, int guestTeamPoints) {
        mPlayer = player;
        mPenaltyCardType = penaltyCardType;
        mSetIndex = setIndex;
        mHomeTeamPoints = homeTeamPoints;
        mGuestTeamPoints = guestTeamPoints;
    }

    public int getPlayer() {
        return mPlayer;
    }

    public PenaltyCardType getPenaltyCardType() {
        return mPenaltyCardType;
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
        } else if (obj instanceof PenaltyCard) {
            PenaltyCard other = (PenaltyCard) obj;
            result = (this.getPlayer() == other.getPlayer())
                    && (this.getPenaltyCardType().equals(other.getPenaltyCardType()))
                    && (this.getSetIndex() == other.getSetIndex())
                    && (this.getHomeTeamPoints() == other.getHomeTeamPoints())
                    && (this.getGuestTeamPoints() == other.getGuestTeamPoints());
        }

        return result;
    }
}
