package com.tonkar.volleyballreferee.engine.team.player;

import com.google.gson.annotations.SerializedName;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
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

    public int getNumber() {
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

}
