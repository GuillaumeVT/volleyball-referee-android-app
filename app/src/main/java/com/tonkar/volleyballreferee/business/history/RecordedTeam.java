package com.tonkar.volleyballreferee.business.history;

import java.util.Set;
import java.util.TreeSet;

public class RecordedTeam {

    private String       mName;
    private int          mColor;
    private int          mLiberoColor;
    private Set<Integer> mPlayers;
    private Set<Integer> mLiberos;


    RecordedTeam() {
        mName = "";
        mColor = -1; // white
        mLiberoColor = -1; // white
        mPlayers = new TreeSet<>();
        mLiberos = new TreeSet<>();
    }

    String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    int getLiberoColor() {
        return mLiberoColor;
    }

    public void setLiberoColor(int liberoColor) {
        mLiberoColor = liberoColor;
    }

    public Set<Integer> getPlayers() {
        return mPlayers;
    }

    public Set<Integer> getLiberos() {
        return mLiberos;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedTeam) {
            RecordedTeam other = (RecordedTeam) obj;
            result = (this.getColor() == other.getColor())
                    && (this.getLiberoColor() == other.getLiberoColor())
                    && this.getName().equals(other.getName())
                    && this.getPlayers().equals(other.getPlayers())
                    && this.getLiberos().equals(other.getLiberos());
        }

        return result;
    }
}
