package com.tonkar.volleyballreferee.engine.team.player;

import com.tonkar.volleyballreferee.engine.game.GameType;

public class SnowPlayer extends Player {

    public SnowPlayer(final int number) {
        super(number);
    }

    // For GSON Deserialization
    public SnowPlayer() {
        this(-1);
    }

    @Override
    public void turnToNextPosition() {
        setPosition(getPosition().nextPosition(GameType.SNOW));
    }

    @Override
    public void turnToPreviousPosition() {
        setPosition(getPosition().previousPosition(GameType.SNOW));
    }

}
