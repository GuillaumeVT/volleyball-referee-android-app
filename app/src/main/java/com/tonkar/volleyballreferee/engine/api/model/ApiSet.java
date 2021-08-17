package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ApiSet {
    @SerializedName("duration")
    private long                  duration;
    @SerializedName("startTime")
    private long                  startTime;
    @SerializedName("endTime")
    private long                  endTime;
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

    public ApiSet() {
        this.ladder = new ArrayList<>();
        this.homeCurrentPlayers = new ApiCourt();
        this.guestCurrentPlayers = new ApiCourt();
        this.homeStartingPlayers = new ApiCourt();
        this.guestStartingPlayers = new ApiCourt();
        this.homeSubstitutions = new ArrayList<>();
        this.guestSubstitutions = new ArrayList<>();
        this.homeCalledTimeouts = new ArrayList<>();
        this.guestCalledTimeouts = new ArrayList<>();
    }

    public int getPoints(TeamType teamType) {
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

    public int getTimeouts(TeamType teamType) {
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

    public int getGameCaptain(TeamType teamType) {
        int number;

        if (TeamType.HOME.equals(teamType)) {
            number = homeCaptain;
        } else {
            number = guestCaptain;
        }

        return number;
    }

    public void setGameCaptain(TeamType teamType, int number) {
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