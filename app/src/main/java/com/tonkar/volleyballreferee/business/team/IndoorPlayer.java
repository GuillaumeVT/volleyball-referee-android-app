package com.tonkar.volleyballreferee.business.team;

public class IndoorPlayer extends Player {

    public IndoorPlayer(final int number) {
        super(number);
    }

    @Override
    public void turnToNextPosition() {
        setPosition(getPosition().nextPosition());
    }

    @Override
    public void turnToPreviousPosition() {
        setPosition(getPosition().previousPosition());
    }
}
