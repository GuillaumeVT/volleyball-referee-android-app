package com.tonkar.volleyballreferee.interfaces;

public class Substitution {

    private int mPlayerIn;
    private int mPlayerOut;

    public Substitution() {
        mPlayerIn = 0;
        mPlayerOut = 0;
    }

    public Substitution(int playerIn, int playerOut) {
        mPlayerIn = playerIn;
        mPlayerOut = playerOut;
    }

    public int getPlayerIn() {
        return mPlayerIn;
    }

    public int getPlayerOut() {
        return mPlayerOut;
    }

    public void setPlayerIn(int playerIn) {
        mPlayerIn = playerIn;
    }

    public void setPlayerOut(int playerOut) {
        mPlayerOut = playerOut;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Substitution) {
            Substitution other = (Substitution) obj;
            result = (this.getPlayerIn() == other.getPlayerIn()) && (this.getPlayerOut() == other.getPlayerOut());
        }

        return result;
    }
}
