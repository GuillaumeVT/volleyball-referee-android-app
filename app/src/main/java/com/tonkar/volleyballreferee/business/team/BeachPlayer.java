package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.PositionType;

public class BeachPlayer extends Player {

    public BeachPlayer(final int number) {
        super(number);
    }

    // For GSON Deserialization
    public BeachPlayer() {
        this(-1);
    }

    @Override
    public void turnToNextPosition() {
        turn();
    }

    @Override
    public void turnToPreviousPosition() {
        turn();
    }

    private void turn() {
        if (PositionType.POSITION_1.equals(getPosition())) {
            setPosition(PositionType.POSITION_2);
        } else if (PositionType.POSITION_2.equals(getPosition())) {
            setPosition(PositionType.POSITION_1);
        }
    }

}
