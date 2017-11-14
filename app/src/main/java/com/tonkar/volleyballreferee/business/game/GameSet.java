package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameSet implements Serializable {

    private final int            mPointsPerSetSettings;
    private       int            mHomeTeamPoints;
    private       int            mGuestTeamPoints;
    private       int            mHomeTeamTimeouts;
    private       int            mGuestTeamTimeouts;
    private final List<TeamType> mPointsLadder;
    private       TeamType       mServingTeamAtStart;
    private       long           mStartTime;
    private       long           mEndTime;

    public GameSet(final int pointsPerSetSettings, final int teamTimeoutsPerSetSettings, final TeamType servingTeamAtStart) {
        mPointsPerSetSettings = pointsPerSetSettings;

        mHomeTeamPoints = 0;
        mGuestTeamPoints = 0;
        mPointsLadder = new ArrayList<>();

        mHomeTeamTimeouts = teamTimeoutsPerSetSettings;
        mGuestTeamTimeouts = teamTimeoutsPerSetSettings;

        mServingTeamAtStart = servingTeamAtStart;

        mStartTime = 0L;
        mEndTime = 0L;
    }

    String getSetSummary() {
        return mHomeTeamPoints + "-" + mGuestTeamPoints;
    }

    public boolean isSetCompleted() {
        // Set is complete when a team reaches the number of points to win (e.g. 25, 21, 15) or more, with a 2-points difference
        return (mHomeTeamPoints >= mPointsPerSetSettings || mGuestTeamPoints >= mPointsPerSetSettings) && (Math.abs(mHomeTeamPoints - mGuestTeamPoints) >= 2);
    }

    public boolean isSetPoint() {
        // Set ball when a team will reach the number of points to win  with 1 point (e.g. 25, 21, 15) or more, with at least 1-point difference
        return (mHomeTeamPoints+1 >= mPointsPerSetSettings || mGuestTeamPoints+1 >= mPointsPerSetSettings) && (Math.abs(mHomeTeamPoints - mGuestTeamPoints) >= 1);
    }

    public TeamType getLeadingTeam() {
        return mGuestTeamPoints > mHomeTeamPoints ? TeamType.GUEST : TeamType.HOME;
    }

    public int addPoint(final TeamType teamType) {
        int points = 0;

        if (mHomeTeamPoints == 0 && mGuestTeamPoints == 0) {
            mStartTime = System.currentTimeMillis();
        }

        switch (teamType) {
            case HOME:
                mHomeTeamPoints++;
                points = mHomeTeamPoints;
                break;
            case GUEST:
                mGuestTeamPoints++;
                points = mGuestTeamPoints;
                break;
        }

        mPointsLadder.add(teamType);

        if (isSetCompleted()) {
            mEndTime = System.currentTimeMillis();
        }

        return points;
    }

    public TeamType removeLastPoint() {
        TeamType teamLosingOnePoint;

        if (mPointsLadder.isEmpty()) {
            teamLosingOnePoint = null;
        } else {
            teamLosingOnePoint = mPointsLadder.get(mPointsLadder.size() - 1);
            mPointsLadder.remove(mPointsLadder.size() - 1);

            switch (teamLosingOnePoint) {
                case HOME:
                    mHomeTeamPoints--;
                    break;
                case GUEST:
                    mGuestTeamPoints--;
                    break;
            }
        }

        return teamLosingOnePoint;
    }

    int getPoints(final TeamType teamType) {
        int points = 0;

        switch (teamType) {
            case HOME:
                points = mHomeTeamPoints;
                break;
            case GUEST:
                points = mGuestTeamPoints;
                break;
        }

        return points;
    }

    public List<TeamType> getPointsLadder() {
        return new ArrayList<>(mPointsLadder);
    }

    int getTimeouts(final TeamType teamType) {
        int timeouts = 0;

        switch (teamType) {
            case HOME:
                timeouts = mHomeTeamTimeouts;
                break;
            case GUEST:
                timeouts = mGuestTeamTimeouts;
                break;
        }

        return timeouts;
    }

    int removeTimeout(final TeamType teamType) {
        int timeouts = 0;

        switch (teamType) {
            case HOME:
                mHomeTeamTimeouts--;
                timeouts = mHomeTeamTimeouts;
                break;
            case GUEST:
                mGuestTeamTimeouts--;
                timeouts = mGuestTeamTimeouts;
                break;
        }

        return timeouts;
    }

    public TeamType getServingTeam() {
        final TeamType servingTeam;

        if (mPointsLadder.isEmpty()) {
            servingTeam = mServingTeamAtStart;
        } else {
            servingTeam = mPointsLadder.get(mPointsLadder.size() - 1);
        }

        return servingTeam;
    }

    void setServingTeamAtStart(TeamType servingTeamAtStart) {
        mServingTeamAtStart = servingTeamAtStart;
    }

    long getDuration() {
        long duration;

        if (mStartTime > 0L) {
            if (mEndTime > 0L) {
                duration = mEndTime - mStartTime;
            } else {
                duration = System.currentTimeMillis() - mStartTime;
            }
        } else {
            duration = 0L;
        }

        return duration;
    }

}
