package com.tonkar.volleyballreferee.business.game;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameService;

public abstract class BaseGame implements GameService {

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
        } else if (obj instanceof BaseGame) {
            BaseGame other = (BaseGame) obj;
            result = this.getClassType().equals(other.getClassType());
        }

        return result;
    }
}
