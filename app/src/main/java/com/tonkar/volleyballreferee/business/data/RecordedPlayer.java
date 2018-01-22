package com.tonkar.volleyballreferee.business.data;

import com.tonkar.volleyballreferee.interfaces.PositionType;

public class RecordedPlayer {

    private int          mNumber;
    private PositionType mPositionType;

    public RecordedPlayer() {
        mNumber = 0;
        mPositionType = PositionType.BENCH;
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public PositionType getPositionType() {
        return mPositionType;
    }

    public void setPositionType(PositionType positionType) {
        mPositionType = positionType;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedPlayer) {
            RecordedPlayer other = (RecordedPlayer) obj;
            result = (this.getNumber() == other.getNumber())
                    && this.getPositionType().equals(other.getPositionType());
        }

        return result;
    }
}
