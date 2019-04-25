package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @Getter @Setter @EqualsAndHashCode
public class ApiSet {

    @SerializedName("duration")
    private long                  duration;
    @SerializedName("homePoints")
    private int                   homePoints;
    @SerializedName("guestPoints")
    private int                   guestPoints;
    @SerializedName("homeTimeouts")
    private int                   homeTimeouts;
    @SerializedName("guestTimeouts")
    private int                   guestTimeouts;
    @SerializedName("ladder")
    private List<TeamType>        ladder;
    @SerializedName("serving")
    private TeamType              serving;
    @SerializedName("firstServing")
    private TeamType              firstServing;
    @SerializedName("homeCurrentPlayers")
    private ApiCourt              homeCurrentPlayers;
    @SerializedName("guestCurrentPlayers")
    private ApiCourt              guestCurrentPlayers;
    @SerializedName("homeStartingPlayers")
    private ApiCourt              homeStartingPlayers;
    @SerializedName("guestStartingPlayers")
    private ApiCourt              guestStartingPlayers;
    @SerializedName("homeSubstitutions")
    private List<ApiSubstitution> homeSubstitutions;
    @SerializedName("guestSubstitutions")
    private List<ApiSubstitution> guestSubstitutions;
    @SerializedName("homeCaptain")
    private int                   homeCaptain;
    @SerializedName("guestCaptain")
    private int                   guestCaptain;
    @SerializedName("homeCalledTimeouts")
    private List<ApiTimeout>      homeCalledTimeouts;
    @SerializedName("guestCalledTimeouts")
    private List<ApiTimeout>      guestCalledTimeouts;
    @SerializedName("remainingTime")
    private long                  remainingTime;

    int getPoints(TeamType teamType) {
        int points;

        if (TeamType.HOME.equals(teamType)) {
            points = homePoints;
        } else {
            points = guestPoints;
        }

        return points;
    }

    public void setPoints(TeamType teamType, int points) {
        if (TeamType.HOME.equals(teamType)) {
            homePoints = points;
        } else {
            guestPoints = points;
        }
    }

    int getTimeouts(TeamType teamType) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = homeTimeouts;
        } else {
            count = guestTimeouts;
        }

        return count;
    }

    public void setTimeouts(TeamType teamType, int count) {
        if (TeamType.HOME.equals(teamType)) {
            homeTimeouts = count;
        } else {
            guestTimeouts = count;
        }
    }

    public ApiCourt getCurrentPlayers(TeamType teamType) {
        ApiCourt players;

        if (TeamType.HOME.equals(teamType)) {
            players = homeCurrentPlayers;
        } else {
            players = guestCurrentPlayers;
        }

        return players;
    }

    public ApiCourt getStartingPlayers(TeamType teamType) {
        ApiCourt players;

        if (TeamType.HOME.equals(teamType)) {
            players = homeStartingPlayers;
        } else {
            players = guestStartingPlayers;
        }

        return players;
    }

    public List<ApiSubstitution> getSubstitutions(TeamType teamType) {
        List<ApiSubstitution> substitutions;

        if (TeamType.HOME.equals(teamType)) {
            substitutions = homeSubstitutions;
        } else {
            substitutions = guestSubstitutions;
        }

        return substitutions;
    }

    public int getActingCaptain(TeamType teamType) {
        int number;

        if (TeamType.HOME.equals(teamType)) {
            number = homeCaptain;
        } else {
            number = guestCaptain;
        }

        return number;
    }

    public void setActingCaptain(TeamType teamType, int number) {
        if (TeamType.HOME.equals(teamType)) {
            homeCaptain = number;
        } else {
            guestCaptain = number;
        }
    }

    public List<ApiTimeout> getCalledTimeouts(TeamType teamType) {
        List<ApiTimeout> timeouts;

        if (TeamType.HOME.equals(teamType)) {
            timeouts = homeCalledTimeouts;
        } else {
            timeouts = guestCalledTimeouts;
        }

        return timeouts;
    }

}