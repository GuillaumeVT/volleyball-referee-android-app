package com.tonkar.volleyballreferee.business.team;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;

public abstract class Player {

    @SerializedName("classType")
    private       String       mClassType;
    @SerializedName("num")
    private final int          mNumber;
    @SerializedName("pos")
    private       PositionType mCurrentPosition;

    Player(final int number) {
        mClassType = getClass().getName();
        mNumber = number;
        mCurrentPosition = PositionType.BENCH;
    }

    int getNumber() {
        return mNumber;
    }

    public PositionType getPosition() {
        return mCurrentPosition;
    }

    public void setPosition(final PositionType position) {
        mCurrentPosition = position;
    }

    public abstract void turnToNextPosition();

    public abstract void turnToPreviousPosition();

    private String getClassType() {
        return mClassType;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Player) {
            Player other = (Player) obj;
            result = (this.getClassType().equals(other.getClassType()))
                    && (this.getNumber() == other.getNumber())
                    && (this.getPosition().equals(other.getPosition()));
        }

        return result;
    }

}
