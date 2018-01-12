package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.TeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.Timeout;
import com.tonkar.volleyballreferee.rules.Rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Set implements Serializable {

    private final int             mPointsToWinSet;
    private       int             mHomeTeamPoints;
    private       int             mGuestTeamPoints;
    private       int             mHomeTeamRemainingTimeouts;
    private       int             mGuestTeamRemainingTimeouts;
    private final List<TeamType>  mPointsLadder;
    private       TeamType        mServingTeamAtStart;
    private       long            mStartTime;
    private       long            mEndTime;
    private       TeamComposition mHomeTeamComposition;
    private       TeamComposition mGuestTeamComposition;
    private final List<Timeout>   mHomeTeamCalledTimeouts;
    private final List<Timeout>   mGuestTeamCalledTimeouts;

    public Set(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        mPointsToWinSet = pointsToWinSet;

        mHomeTeamPoints = 0;
        mGuestTeamPoints = 0;
        mPointsLadder = new ArrayList<>();

        mHomeTeamRemainingTimeouts = rules.getTeamTimeoutsPerSet();
        mGuestTeamRemainingTimeouts = rules.getTeamTimeoutsPerSet();

        mServingTeamAtStart = servingTeamAtStart;

        mStartTime = 0L;
        mEndTime = 0L;

        mHomeTeamCalledTimeouts = new ArrayList<>();
        mGuestTeamCalledTimeouts = new ArrayList<>();
    }

    protected abstract TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition);

    void createTeams(Rules rules, TeamDefinition homeTeamDefinition, TeamDefinition guestTeamDefinition) {
        mHomeTeamComposition = createTeamComposition(rules, homeTeamDefinition);
        mGuestTeamComposition = createTeamComposition(rules, guestTeamDefinition);
    }

    boolean areTeamsCreated() {
        return mHomeTeamComposition != null && mGuestTeamComposition != null;
    }

    String getSetSummary() {
        return mHomeTeamPoints + "-" + mGuestTeamPoints;
    }

    public boolean isSetCompleted() {
        // Set is complete when a team reaches the number of points to win (e.g. 25, 21, 15) or more, with a 2-points difference
        return (mHomeTeamPoints >= mPointsToWinSet || mGuestTeamPoints >= mPointsToWinSet) && (Math.abs(mHomeTeamPoints - mGuestTeamPoints) >= 2);
    }

    public boolean isSetPoint() {
        // Set ball when a team will reach the number of points to win  with 1 point (e.g. 25, 21, 15) or more, with at least 1-point difference
        return !isSetCompleted() && (mHomeTeamPoints+1 >= mPointsToWinSet || mGuestTeamPoints+1 >= mPointsToWinSet) && (Math.abs(mHomeTeamPoints - mGuestTeamPoints) >= 1);
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

    int getRemainingTimeouts(final TeamType teamType) {
        int timeouts = 0;

        switch (teamType) {
            case HOME:
                timeouts = mHomeTeamRemainingTimeouts;
                break;
            case GUEST:
                timeouts = mGuestTeamRemainingTimeouts;
                break;
        }

        return timeouts;
    }

    List<Timeout> getCalledTimeouts(final TeamType teamType) {
        List<Timeout> timeouts = new ArrayList<>();

        switch (teamType) {
            case HOME:
                timeouts = new ArrayList<>(mHomeTeamCalledTimeouts);
                break;
            case GUEST:
                timeouts = new ArrayList<>(mGuestTeamCalledTimeouts);
                break;
        }

        return timeouts;
    }

    int removeTimeout(final TeamType teamType) {
        int timeouts = 0;

        switch (teamType) {
            case HOME:
                mHomeTeamRemainingTimeouts--;
                timeouts = mHomeTeamRemainingTimeouts;
                mHomeTeamCalledTimeouts.add(new Timeout(mHomeTeamPoints, mGuestTeamPoints));
                break;
            case GUEST:
                mGuestTeamRemainingTimeouts--;
                timeouts = mGuestTeamRemainingTimeouts;
                mGuestTeamCalledTimeouts.add(new Timeout(mHomeTeamPoints, mGuestTeamPoints));
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

    TeamType getServingTeamAtStart() {
        return mServingTeamAtStart;
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

    TeamComposition getTeamComposition(final TeamType teamType) {
        TeamComposition teamComposition;

        if (TeamType.HOME.equals(teamType)) {
            teamComposition = mHomeTeamComposition;
        } else {
            teamComposition = mGuestTeamComposition;
        }

        return teamComposition;
    }

}
