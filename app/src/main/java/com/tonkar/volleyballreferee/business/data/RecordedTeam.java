package com.tonkar.volleyballreferee.business.data;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;

import java.util.Set;
import java.util.TreeSet;

public class RecordedTeam {

    @SerializedName("name")
    private String       mName;
    @SerializedName("color")
    private String       mColor;
    @SerializedName("liberoColor")
    private String       mLiberoColor;
    @SerializedName("players")
    private Set<Integer> mPlayers;
    @SerializedName("liberos")
    private Set<Integer> mLiberos;
    @SerializedName("captain")
    private int          mCaptain;
    @SerializedName("gender")
    private GenderType   mGenderType;

    RecordedTeam() {
        mName = "";
        mColor = "#ffffff"; // white
        mLiberoColor = "#ffffff"; // white
        mPlayers = new TreeSet<>();
        mLiberos = new TreeSet<>();
        mCaptain = -1;
        mGenderType = GenderType.MIXED;
    }

    String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    int getColor() {
        return Color.parseColor(mColor);
    }

    public void setColor(int color) {
        mColor = colorIntToHtml(color);
    }

    int getLiberoColor() {
        return Color.parseColor(mLiberoColor);
    }

    public void setLiberoColor(int liberoColor) {
        mLiberoColor = colorIntToHtml(liberoColor);
    }

    public Set<Integer> getPlayers() {
        return mPlayers;
    }

    public Set<Integer> getLiberos() {
        return mLiberos;
    }

    public void setCaptain(int number) {
        mCaptain = number;
    }

    int getCaptain() {
        return mCaptain;
    }

    public GenderType getGenderType() {
        return mGenderType;
    }

    public void setGenderType(GenderType genderType) {
        mGenderType = genderType;
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
                    && this.getGenderType().equals(other.getGenderType())
                    && this.getPlayers().equals(other.getPlayers())
                    && this.getLiberos().equals(other.getLiberos())
                    && (this.getCaptain() == other.getCaptain());
        }

        return result;
    }

    private String colorIntToHtml(int color) {
        return String.format("#%06X", (0xFFFFFF & color)).toLowerCase();
    }
}
