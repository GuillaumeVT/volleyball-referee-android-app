package com.tonkar.volleyballreferee.business.data;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.Timeout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordedSet {

    @SerializedName("duration")
    private       long                 mDuration;
    @SerializedName("hPoints")
    private       int                  mHomeTeamPoints;
    @SerializedName("gPoints")
    private       int                  mGuestTeamPoints;
    @SerializedName("hTimeouts")
    private       int                  mHomeTeamTimeouts;
    @SerializedName("gTimeouts")
    private       int                  mGuestTeamTimeouts;
    @SerializedName("ladder")
    private final List<TeamType>       mPointsLadder;
    @SerializedName("serving")
    private       TeamType             mServingTeam;
    @SerializedName("hCurrentPlayers")
    private final List<RecordedPlayer> mHomeTeamCurrentPlayers;
    @SerializedName("gCurrentPlayers")
    private final List<RecordedPlayer> mGuestTeamCurrentPlayers;
    @SerializedName("hStartingPlayers")
    private final List<RecordedPlayer> mHomeTeamStartingPlayers;
    @SerializedName("gStartingPlayers")
    private final List<RecordedPlayer> mGuestTeamStartingPlayers;
    @SerializedName("hSubstitutions")
    private final List<Substitution>   mHomeTeamSubstitutions;
    @SerializedName("gSubstitutions")
    private final List<Substitution>   mGuestTeamSubstitutions;
    @SerializedName("hCaptain")
    private       int                  mHomeTeamActingCaptain;
    @SerializedName("gCaptain")
    private       int                  mGuestTeamActingCaptain;
    @SerializedName("hCalledTimeouts")
    private final List<Timeout>        mHomeTeamCalledTimeouts;
    @SerializedName("gCalledTimeouts")
    private final List<Timeout>        mGuestTeamCalledTimeouts;
    @SerializedName("rTime")
    private       long                 mRemainingTime;

    public RecordedSet() {
        mDuration = 0L;
        mHomeTeamPoints = 0;
        mGuestTeamPoints = 0;
        mHomeTeamTimeouts = 0;
        mGuestTeamTimeouts = 0;
        mPointsLadder = new ArrayList<>();
        mServingTeam = TeamType.HOME;
        mHomeTeamCurrentPlayers = new ArrayList<>();
        mGuestTeamCurrentPlayers = new ArrayList<>();
        mHomeTeamStartingPlayers = new ArrayList<>();
        mGuestTeamStartingPlayers = new ArrayList<>();
        mHomeTeamSubstitutions = new ArrayList<>();
        mGuestTeamSubstitutions = new ArrayList<>();
        mHomeTeamActingCaptain = 0;
        mGuestTeamActingCaptain = 0;

        mHomeTeamCalledTimeouts = new ArrayList<>();
        mGuestTeamCalledTimeouts = new ArrayList<>();
        mRemainingTime = 0L;
    }

    long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    int getPoints(TeamType teamType) {
        int points;

        if (TeamType.HOME.equals(teamType)) {
            points = mHomeTeamPoints;
        } else {
            points = mGuestTeamPoints;
        }

        return points;
    }

    public void setPoints(TeamType teamType, int points) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeTeamPoints = points;
        } else {
            mGuestTeamPoints = points;
        }
    }

    int getTimeouts(TeamType teamType) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = mHomeTeamTimeouts;
        } else {
            count = mGuestTeamTimeouts;
        }

        return count;
    }

    public void setTimeouts(TeamType teamType, int count) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeTeamTimeouts = count;
        } else {
            mGuestTeamTimeouts = count;
        }
    }

    public List<TeamType> getPointsLadder() {
        return mPointsLadder;
    }

    TeamType getServingTeam() {
        return mServingTeam;
    }

    public void setServingTeam(TeamType servingTeam) {
        mServingTeam = servingTeam;
    }

    public List<RecordedPlayer> getCurrentPlayers(TeamType teamType) {
        List<RecordedPlayer> players;

        if (TeamType.HOME.equals(teamType)) {
            players = mHomeTeamCurrentPlayers;
        } else {
            players = mGuestTeamCurrentPlayers;
        }

        return players;
    }

    public List<RecordedPlayer> getStartingPlayers(TeamType teamType) {
        List<RecordedPlayer> players;

        if (TeamType.HOME.equals(teamType)) {
            players = mHomeTeamStartingPlayers;
        } else {
            players = mGuestTeamStartingPlayers;
        }

        return players;
    }

    public List<Substitution> getSubstitutions(TeamType teamType) {
        List<Substitution> substitutions;

        if (TeamType.HOME.equals(teamType)) {
            substitutions = mHomeTeamSubstitutions;
        } else {
            substitutions = mGuestTeamSubstitutions;
        }

        return substitutions;
    }

    int getActingCaptain(TeamType teamType) {
        int number;

        if (TeamType.HOME.equals(teamType)) {
            number = mHomeTeamActingCaptain;
        } else {
            number = mGuestTeamActingCaptain;
        }

        return number;
    }

    public void setActingCaptain(TeamType teamType, int number) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeTeamActingCaptain = number;
        } else {
            mGuestTeamActingCaptain = number;
        }
    }

    public List<Timeout> getCalledTimeouts(TeamType teamType) {
        List<Timeout> timeouts;

        if (TeamType.HOME.equals(teamType)) {
            timeouts = mHomeTeamCalledTimeouts;
        } else {
            timeouts = mGuestTeamCalledTimeouts;
        }

        return timeouts;
    }

    long getRemainingTime() {
        return mRemainingTime;
    }

    void setRemainingTime(long remainingTime) {
        mRemainingTime = remainingTime;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedSet) {
            RecordedSet other = (RecordedSet) obj;
            result = (this.getDuration() == other.getDuration())
                    && (this.getPoints(TeamType.HOME) == other.getPoints(TeamType.HOME))
                    && (this.getPoints(TeamType.GUEST) == other.getPoints(TeamType.GUEST))
                    && (this.getTimeouts(TeamType.HOME) == other.getTimeouts(TeamType.HOME))
                    && (this.getTimeouts(TeamType.GUEST) == other.getTimeouts(TeamType.GUEST))
                    && this.getPointsLadder().equals(other.getPointsLadder())
                    && this.getServingTeam().equals(other.getServingTeam())
                    && this.getCurrentPlayers(TeamType.HOME).equals(other.getCurrentPlayers(TeamType.HOME))
                    && this.getCurrentPlayers(TeamType.GUEST).equals(other.getCurrentPlayers(TeamType.GUEST))
                    && this.getStartingPlayers(TeamType.HOME).equals(other.getStartingPlayers(TeamType.HOME))
                    && this.getStartingPlayers(TeamType.GUEST).equals(other.getStartingPlayers(TeamType.GUEST))
                    && this.getSubstitutions(TeamType.HOME).equals(other.getSubstitutions(TeamType.HOME))
                    && this.getSubstitutions(TeamType.GUEST).equals(other.getSubstitutions(TeamType.GUEST))
                    && (this.getActingCaptain(TeamType.HOME) == other.getActingCaptain(TeamType.HOME))
                    && (this.getActingCaptain(TeamType.GUEST) == other.getActingCaptain(TeamType.GUEST))
                    && this.getCalledTimeouts(TeamType.HOME).equals(other.getCalledTimeouts(TeamType.HOME))
                    && this.getCalledTimeouts(TeamType.GUEST).equals(other.getCalledTimeouts(TeamType.GUEST))
                    && (this.getRemainingTime() == other.getRemainingTime());
        }

        return result;
    }
}
