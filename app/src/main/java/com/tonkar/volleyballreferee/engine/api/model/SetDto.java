package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.*;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class SetDto {
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
    private CourtDto              homeCurrentPlayers;
    @SerializedName("guestCurrentPlayers")
    private CourtDto              guestCurrentPlayers;
    @SerializedName("homeStartingPlayers")
    private CourtDto              homeStartingPlayers;
    @SerializedName("guestStartingPlayers")
    private CourtDto              guestStartingPlayers;
    @SerializedName("homeSubstitutions")
    private List<SubstitutionDto> homeSubstitutions;
    @SerializedName("guestSubstitutions")
    private List<SubstitutionDto> guestSubstitutions;
    @SerializedName("homeCaptain")
    private int                   homeCaptain;
    @SerializedName("guestCaptain")
    private int                   guestCaptain;
    @SerializedName("homeCalledTimeouts")
    private List<TimeoutDto>      homeCalledTimeouts;
    @SerializedName("guestCalledTimeouts")
    private List<TimeoutDto>      guestCalledTimeouts;
    @SerializedName("remainingTime")
    private long                  remainingTime;

    public SetDto() {
        this.ladder = new ArrayList<>();
        this.homeCurrentPlayers = new CourtDto();
        this.guestCurrentPlayers = new CourtDto();
        this.homeStartingPlayers = new CourtDto();
        this.guestStartingPlayers = new CourtDto();
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

    public CourtDto getCurrentPlayers(TeamType teamType) {
        CourtDto players;

        if (TeamType.HOME.equals(teamType)) {
            players = homeCurrentPlayers;
        } else {
            players = guestCurrentPlayers;
        }

        return players;
    }

    public CourtDto getStartingPlayers(TeamType teamType) {
        CourtDto players;

        if (TeamType.HOME.equals(teamType)) {
            players = homeStartingPlayers;
        } else {
            players = guestStartingPlayers;
        }

        return players;
    }

    public List<SubstitutionDto> getSubstitutions(TeamType teamType) {
        List<SubstitutionDto> substitutions;

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

    public List<TimeoutDto> getCalledTimeouts(TeamType teamType) {
        List<TimeoutDto> timeouts;

        if (TeamType.HOME.equals(teamType)) {
            timeouts = homeCalledTimeouts;
        } else {
            timeouts = guestCalledTimeouts;
        }

        return timeouts;
    }

}