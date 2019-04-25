package com.tonkar.volleyballreferee.business.team;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.api.ApiPlayer;
import com.tonkar.volleyballreferee.api.ApiTeam;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.Set;

public abstract class TeamDefinition extends ApiTeam {

    public static final String DEFAULT_COLOR = "#633303";

    @SerializedName("classType")
    private       String   mClassType;
    @SerializedName("team")
    private final TeamType mTeamType;

    public TeamDefinition(final String createdBy, final GameType gameType, final TeamType teamType) {
        super();
        mClassType = getClass().getName();
        mTeamType = teamType;
        setCreatedBy(createdBy);
        setKind(gameType);
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    public int getNumberOfPlayers() {
        return getPlayers().size();
    }

    public void addPlayer(final int number) {
        Log.i(Tags.TEAM, String.format("Add player #%d to %s team", number, mTeamType.toString()));
        getPlayers().add(new ApiPlayer(number, ""));
    }

    public void removePlayer(final int number) {
        Log.i(Tags.TEAM, String.format("Remove player #%d from %s team", number, mTeamType.toString()));
        getPlayers().remove(new ApiPlayer(number, ""));
    }

    public boolean hasPlayer(final int number) {
        return getPlayers().contains(new ApiPlayer(number, ""));
    }

    public int getExpectedNumberOfPlayersOnCourt() {
        int number;

        switch (getKind()) {
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

    public abstract boolean isLibero(int number);

    public abstract boolean canAddLibero();

    public abstract void addLibero(int number);

    public abstract void removeLibero(int number);

    public abstract boolean isCaptain(int number);

    public abstract Set<Integer> getPossibleCaptains();

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof TeamDefinition) {
            TeamDefinition other = (TeamDefinition) obj;
            result = super.equals(other) &&
                    mClassType.equals(other.mClassType)
                    && mTeamType.equals(other.mTeamType);
        }

        return result;
    }

}
