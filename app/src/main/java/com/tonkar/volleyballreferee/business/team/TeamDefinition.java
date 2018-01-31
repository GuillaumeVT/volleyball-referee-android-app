package com.tonkar.volleyballreferee.business.team;

import android.graphics.Color;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public abstract class TeamDefinition implements Serializable {

    @SerializedName("name")
    private       String       mName;
    @SerializedName("team")
    private final TeamType     mTeamType;
    @SerializedName("color")
    private       String       mColor;
    @SerializedName("players")
    private final Set<Integer> mPlayers;
    @SerializedName("gender")
    private       GenderType   mGenderType;

    public TeamDefinition(final TeamType teamType) {
        mName = "";
        mTeamType = teamType;
        mColor = colorIntToHtml(Integer.MIN_VALUE);
        mPlayers = new TreeSet<>();
        mGenderType = GenderType.MIXED;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
        Log.i("VBR-Team", String.format("%s team name is %s", mTeamType.toString(), mName));
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    public Set<Integer> getPlayers() {
        return new TreeSet<>(mPlayers);
    }

    public int getNumberOfPlayers() {
        return mPlayers.size();
    }

    public void addPlayer(final int number) {
        Log.i("VBR-Team", String.format("Add player #%d to %s team", number, mTeamType.toString()));
        mPlayers.add(number);
    }

    public void removePlayer(final int number) {
        Log.i("VBR-Team", String.format("Remove player #%d from %s team", number, mTeamType.toString()));
        mPlayers.remove(number);
    }

    public boolean hasPlayer(final int number) {
        return mPlayers.contains(number);
    }

    public int getColor() {
        return Color.parseColor(mColor);
    }

    public void setColor(int color) {
        mColor = colorIntToHtml(color);
    }

    public GenderType getGenderType() {
        return mGenderType;
    }

    public void setGenderType(GenderType genderType) {
        mGenderType = genderType;
    }

    String colorIntToHtml(int color) {
        return String.format("#%06X", (0xFFFFFF & color)).toLowerCase();
    }
}
