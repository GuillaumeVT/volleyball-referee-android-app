package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.team.PositionType;

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
        if (PositionType.POSITION_1.equals(getPosition())) {
            setPosition(PositionType.POSITION_4);
        } else if (PositionType.POSITION_4.equals(getPosition())) {
            setPosition(PositionType.POSITION_3);
        } else if (PositionType.POSITION_3.equals(getPosition())) {
            setPosition(PositionType.POSITION_2);
        } else if (PositionType.POSITION_2.equals(getPosition())) {
            setPosition(PositionType.POSITION_1);
        }
    }

    @Override
    public void turnToPreviousPosition() {
        if (PositionType.POSITION_1.equals(getPosition())) {
            setPosition(PositionType.POSITION_2);
        } else if (PositionType.POSITION_2.equals(getPosition())) {
            setPosition(PositionType.POSITION_3);
        } else if (PositionType.POSITION_3.equals(getPosition())) {
            setPosition(PositionType.POSITION_4);
        } else if (PositionType.POSITION_4.equals(getPosition())) {
            setPosition(PositionType.POSITION_1);
        }
    }

}
