package com.tonkar.volleyballreferee.business.team;

import android.graphics.Color;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.Set;
import java.util.TreeSet;

public abstract class TeamDefinition {

    public static final String DEFAULT_COLOR = "#633303";

    @SerializedName("classType")
    private       String       mClassType;
    @SerializedName("kind")
    private final GameType     mGameType;
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

    public TeamDefinition(final GameType gameType, final TeamType teamType) {
        mClassType = getClass().getName();
        mGameType = gameType;
        mName = "";
        mTeamType = teamType;
        mColor = DEFAULT_COLOR;
        mPlayers = new TreeSet<>();
        mGenderType = GenderType.MIXED;
    }

    public GameType getGameType() {
        return mGameType;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
        Log.i(Tags.TEAM, String.format("%s team name is %s", mTeamType.toString(), mName));
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
        Log.i(Tags.TEAM, String.format("Add player #%d to %s team", number, mTeamType.toString()));
        mPlayers.add(number);
    }

    public void removePlayer(final int number) {
        Log.i(Tags.TEAM, String.format("Remove player #%d from %s team", number, mTeamType.toString()));
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

    public int getExpectedNumberOfPlayersOnCourt() {
        int number;

        switch (mGameType) {
            case INDOOR:
                number = 6;
                break;
            case INDOOR_4X4:
                number = 4;
                break;
            case BEACH:
                number = 2;
                break;
            case TIME:
            default:
                number = 0;
                break;
        }

        return number;
    }

    public abstract int getLiberoColor();

    public abstract void setLiberoColor(int color);

    public abstract boolean isLibero(int number);

    public abstract boolean canAddLibero();

    public abstract void addLibero(final int number);

    public abstract void removeLibero(final int number);

    public abstract Set<Integer> getLiberos();

    public abstract void setCaptain(int number);

    public abstract int getCaptain();

    public abstract boolean isCaptain(int number);

    public abstract Set<Integer> getPossibleCaptains();

    String colorIntToHtml(int color) {
        return String.format("#%06X", (0xFFFFFF & color)).toLowerCase();
    }

    private String getClassType() {
        return mClassType;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof TeamDefinition) {
            TeamDefinition other = (TeamDefinition) obj;
            result = (this.getClassType().equals(other.getClassType()))
                    && (this.getName().equals(other.getName()))
                    && (this.getTeamType().equals(other.getTeamType()))
                    && (this.getColor() == other.getColor())
                    && (this.getPlayers().equals(other.getPlayers()))
                    && (this.getGenderType().equals(other.getGenderType()));
        }

        return result;
    }
}
