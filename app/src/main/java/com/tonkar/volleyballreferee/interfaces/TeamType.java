package com.tonkar.volleyballreferee.interfaces;

public enum TeamType {
    HOME, GUEST;

    public TeamType other() {
        if (HOME.equals(this)) {
            return GUEST;
        } else {
            return HOME;
        }
    }
}
