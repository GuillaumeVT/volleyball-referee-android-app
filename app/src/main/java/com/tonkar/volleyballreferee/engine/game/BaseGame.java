package com.tonkar.volleyballreferee.engine.game;

import com.google.gson.annotations.SerializedName;

public abstract class BaseGame implements IGame {

    @SerializedName("classType")
    private String mClassType;

    public BaseGame() {
        mClassType = getClass().getName();
    }

    private String getClassType() {
        return mClassType;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof BaseGame other) {
            result = this.getClassType().equals(other.getClassType());
        }

        return result;
    }
}
