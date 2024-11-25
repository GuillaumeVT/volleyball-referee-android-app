package com.tonkar.volleyballreferee.engine.game.set;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.api.model.TimeoutDto;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.TeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;

import java.util.*;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class Set {

    @SerializedName("classType")
    private       String           mClassType;
    @SerializedName("pointsToWinSet")
    private final int              mPointsToWinSet;
    @SerializedName("twoPointsDifference")
    private final boolean          m2PointsDifference;
    @SerializedName("homePoints")
    private       int              mHomePoints;
    @SerializedName("guestPoints")
    private       int              mGuestPoints;
    @SerializedName("homeRemainingTimeouts")
    private       int              mHomeRemainingTimeouts;
    @SerializedName("guestRemainingTimeouts")
    private       int              mGuestRemainingTimeouts;
    @SerializedName("pointsLadder")
    private final List<TeamType>   mPointsLadder;
    @SerializedName("servingTeamAtStart")
    private       TeamType         mServingTeamAtStart;
    @SerializedName("startTime")
    private       long             mStartTime;
    @SerializedName("endTime")
    private       long             mEndTime;
    @SerializedName("homeComposition")
    private       TeamComposition  mHomeComposition;
    @SerializedName("guestComposition")
    private       TeamComposition  mGuestComposition;
    @SerializedName("homeCalledTimeouts")
    private final List<TimeoutDto> mHomeCalledTimeouts;
    @SerializedName("guestCalledTimeouts")
    private final List<TimeoutDto> mGuestCalledTimeouts;

    protected Set(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        this(rules, pointsToWinSet, servingTeamAtStart, null, null);
    }

    protected Set(Rules rules,
                  int pointsToWinSet,
                  TeamType servingTeamAtStart,
                  TeamDefinition homeTeamDefinition,
                  TeamDefinition guestTeamDefinition) {
        mClassType = getClass().getName();
        mPointsToWinSet = pointsToWinSet;
        m2PointsDifference = rules.isTwoPointsDifference();

        if (homeTeamDefinition != null && guestTeamDefinition != null) {
            mHomeComposition = createTeamComposition(rules, homeTeamDefinition);
            mGuestComposition = createTeamComposition(rules, guestTeamDefinition);
        }

        mHomePoints = 0;
        mGuestPoints = 0;
        mPointsLadder = new ArrayList<>();

        mHomeRemainingTimeouts = rules.getTeamTimeoutsPerSet();
        mGuestRemainingTimeouts = rules.getTeamTimeoutsPerSet();

        mServingTeamAtStart = servingTeamAtStart;

        mStartTime = 0L;
        mEndTime = 0L;

        mHomeCalledTimeouts = new ArrayList<>();
        mGuestCalledTimeouts = new ArrayList<>();
    }

    protected abstract TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition);

    public String getSetSummary() {
        return mHomePoints + "-" + mGuestPoints;
    }

    public boolean isSetCompleted() {
        // Set is complete when a team reaches the number of points to win (e.g. 25, 21, 15) or more, with a 2-points difference
        return (mHomePoints >= mPointsToWinSet || mGuestPoints >= mPointsToWinSet) && (!m2PointsDifference || Math.abs(
                mHomePoints - mGuestPoints) >= 2);
    }

    public boolean isSetPoint() {
        // Set ball when a team will reach the number of points to win  with 1 point (e.g. 25, 21, 15) or more, with at least 1-point difference
        return !isSetCompleted() && (mHomePoints + 1 >= mPointsToWinSet || mGuestPoints + 1 >= mPointsToWinSet) && (!m2PointsDifference || Math.abs(
                mHomePoints - mGuestPoints) >= 1);
    }

    public TeamType getLeadingTeam() {
        return mGuestPoints > mHomePoints ? TeamType.GUEST : TeamType.HOME;
    }

    public int addPoint(final TeamType teamType) {
        int points = 0;

        if (mHomePoints == 0 && mGuestPoints == 0) {
            mStartTime = System.currentTimeMillis();
        }

        switch (teamType) {
            case HOME -> {
                mHomePoints++;
                points = mHomePoints;
            }
            case GUEST -> {
                mGuestPoints++;
                points = mGuestPoints;
            }
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
                case HOME -> mHomePoints--;
                case GUEST -> mGuestPoints--;
            }
        }

        return teamLosingOnePoint;
    }

    public int getPoints(final TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? mHomePoints : mGuestPoints;
    }

    public List<TeamType> getPointsLadder() {
        return new ArrayList<>(mPointsLadder);
    }

    public int getRemainingTimeouts(final TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? mHomeRemainingTimeouts : mGuestRemainingTimeouts;
    }

    public List<TimeoutDto> getCalledTimeouts(final TeamType teamType) {
        return new ArrayList<>(TeamType.HOME.equals(teamType) ? mHomeCalledTimeouts : mGuestCalledTimeouts);
    }

    public int removeTimeout(final TeamType teamType) {
        int timeouts;

        if (TeamType.HOME.equals(teamType)) {
            mHomeRemainingTimeouts--;
            timeouts = mHomeRemainingTimeouts;
            mHomeCalledTimeouts.add(new TimeoutDto(mHomePoints, mGuestPoints));
        } else {
            mGuestRemainingTimeouts--;
            timeouts = mGuestRemainingTimeouts;
            mGuestCalledTimeouts.add(new TimeoutDto(mHomePoints, mGuestPoints));
        }

        return timeouts;
    }

    public int undoTimeout(TeamType teamType) {
        int timeouts = TeamType.HOME.equals(teamType) ? mHomeRemainingTimeouts : mGuestRemainingTimeouts;

        for (TimeoutDto timeout : getCalledTimeouts(teamType)) {
            if (timeout.getHomePoints() == mHomePoints && timeout.getGuestPoints() == mGuestPoints) {
                if (TeamType.HOME.equals(teamType)) {
                    mHomeRemainingTimeouts++;
                    timeouts = mHomeRemainingTimeouts;
                    mHomeCalledTimeouts.remove(timeout);
                } else {
                    mGuestRemainingTimeouts++;
                    timeouts = mGuestRemainingTimeouts;
                    mGuestCalledTimeouts.remove(timeout);
                }
                break;
            }
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

    public void setServingTeamAtStart(TeamType servingTeamAtStart) {
        mServingTeamAtStart = servingTeamAtStart;
    }

    public TeamType getServingTeamAtStart() {
        return mServingTeamAtStart;
    }

    public long getDuration() {
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

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public TeamComposition getTeamComposition(final TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? mHomeComposition : mGuestComposition;
    }

}
