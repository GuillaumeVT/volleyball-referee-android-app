package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.PositionType;

import java.io.Serializable;

abstract class Player implements Serializable {

    private final int          mNumber;
    private       PositionType mCurrentPosition;

    Player(final int number) {
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

}
