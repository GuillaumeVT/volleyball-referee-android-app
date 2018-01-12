package com.tonkar.volleyballreferee.business.history;

import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.Timeout;

import java.util.ArrayList;
import java.util.List;

public class RecordedSet {

    private       long                 mDuration;
    private       int                  mHomeTeamPoints;
    private       int                  mGuestTeamPoints;
    private       int                  mHomeTeamTimeouts;
    private       int                  mGuestTeamTimeouts;
    private final List<TeamType>       mPointsLadder;
    private       TeamType             mServingTeam;
    private final List<RecordedPlayer> mHomeTeamCurrentPlayers;
    private final List<RecordedPlayer> mGuestTeamCurrentPlayers;
    private final List<RecordedPlayer> mHomeTeamStartingPlayers;
    private final List<RecordedPlayer> mGuestTeamStartingPlayers;
    private final List<Substitution>   mHomeTeamSubstitutions;
    private final List<Substitution>   mGuestTeamSubstitutions;
    private       int                  mHomeTeamActingCaptain;
    private       int                  mGuestTeamActingCaptain;
    private final List<Timeout>        mHomeTeamCalledTimeouts;
    private final List<Timeout>        mGuestTeamCalledTimeouts;


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
                    && this.getCalledTimeouts(TeamType.GUEST).equals(other.getCalledTimeouts(TeamType.GUEST));
        }

        return result;
    }
}
