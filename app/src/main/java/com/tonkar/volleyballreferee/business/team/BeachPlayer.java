package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.GameType;

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
        setPosition(getPosition().nextPosition(GameType.BEACH));
    }

    @Override
    public void turnToPreviousPosition() {
        setPosition(getPosition().previousPosition(GameType.BEACH));
    }

}
