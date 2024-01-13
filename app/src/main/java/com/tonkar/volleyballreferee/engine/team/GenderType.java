package com.tonkar.volleyballreferee.engine.team;

public enum GenderType {
    LADIES,
    GENTS,
    MIXED;

    public GenderType next() {
        if (MIXED.equals(this)) {
            return LADIES;
        } else if (LADIES.equals(this)) {
            return GENTS;
        } else {
            return MIXED;
        }
    }
}
