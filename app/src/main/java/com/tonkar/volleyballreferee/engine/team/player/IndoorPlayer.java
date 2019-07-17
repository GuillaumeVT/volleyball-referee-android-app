package com.tonkar.volleyballreferee.engine.team.player;

import com.tonkar.volleyballreferee.engine.game.GameType;

public class IndoorPlayer extends Player {

    public IndoorPlayer(final int number) {
        super(number);
    }

    // For GSON Deserialization
    public IndoorPlayer() {
        this(-1);
    }

    @Override
    public void turnToNextPosition() {
        setPosition(getPosition().nextPosition(GameType.INDOOR));
    }

    @Override
    public void turnToPreviousPosition() {
        setPosition(getPosition().previousPosition(GameType.INDOOR));
    }

}
