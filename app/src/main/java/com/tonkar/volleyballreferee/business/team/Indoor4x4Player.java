package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.GameType;

public class Indoor4x4Player extends Player {

    public Indoor4x4Player(final int number) {
        super(number);
    }

    // For GSON Deserialization
    public Indoor4x4Player() {
        this(-1);
    }

    @Override
    public void turnToNextPosition() {
        setPosition(getPosition().nextPosition(GameType.INDOOR_4X4));
    }

    @Override
    public void turnToPreviousPosition() {
        setPosition(getPosition().previousPosition(GameType.INDOOR_4X4));
    }

}
