package com.tonkar.volleyballreferee.engine.game.set;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTimeout;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.TeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

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
    private final List<ApiTimeout> mHomeCalledTimeouts;
    @SerializedName("guestCalledTimeouts")
    private final List<ApiTimeout> mGuestCalledTimeouts;

    protected Set(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        this(rules, pointsToWinSet, servingTeamAtStart, null, null);
    }

    protected Set(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart, TeamDefinition homeTeamDefinition, TeamDefinition guestTeamDefinition) {
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
        return (mHomePoints >= mPointsToWinSet || mGuestPoints >= mPointsToWinSet) && (!m2PointsDifference || Math.abs(mHomePoints - mGuestPoints) >= 2);
    }

    public boolean isSetPoint() {
        // Set ball when a team will reach the number of points to win  with 1 point (e.g. 25, 21, 15) or more, with at least 1-point difference
        return !isSetCompleted() && (mHomePoints +1 >= mPointsToWinSet || mGuestPoints +1 >= mPointsToWinSet) && (!m2PointsDifference || Math.abs(mHomePoints - mGuestPoints) >= 1);
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
            case HOME:
                mHomePoints++;
                points = mHomePoints;
                break;
            case GUEST:
                mGuestPoints++;
                points = mGuestPoints;
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
                    mHomePoints--;
                    break;
                case GUEST:
                    mGuestPoints--;
                    break;
            }
        }

        return teamLosingOnePoint;
    }

    public int getPoints(final TeamType teamType) {
        int points = 0;

        switch (teamType) {
            case HOME:
                points = mHomePoints;
                break;
            case GUEST:
                points = mGuestPoints;
                break;
        }

        return points;
    }

    public List<TeamType> getPointsLadder() {
        return new ArrayList<>(mPointsLadder);
    }

    public int getRemainingTimeouts(final TeamType teamType) {
        int timeouts = 0;

        switch (teamType) {
            case HOME:
                timeouts = mHomeRemainingTimeouts;
                break;
            case GUEST:
                timeouts = mGuestRemainingTimeouts;
                break;
        }

        return timeouts;
    }

    public List<ApiTimeout> getCalledTimeouts(final TeamType teamType) {
        List<ApiTimeout> timeouts = new ArrayList<>();

        switch (teamType) {
            case HOME:
                timeouts = new ArrayList<>(mHomeCalledTimeouts);
                break;
            case GUEST:
                timeouts = new ArrayList<>(mGuestCalledTimeouts);
                break;
        }

        return timeouts;
    }

    public int removeTimeout(final TeamType teamType) {
        int timeouts = 0;

        switch (teamType) {
            case HOME:
                mHomeRemainingTimeouts--;
                timeouts = mHomeRemainingTimeouts;
                mHomeCalledTimeouts.add(new ApiTimeout(mHomePoints, mGuestPoints));
                break;
            case GUEST:
                mGuestRemainingTimeouts--;
                timeouts = mGuestRemainingTimeouts;
                mGuestCalledTimeouts.add(new ApiTimeout(mHomePoints, mGuestPoints));
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

    public TeamComposition getTeamComposition(final TeamType teamType) {
        TeamComposition teamComposition;

        if (TeamType.HOME.equals(teamType)) {
            teamComposition = mHomeComposition;
        } else {
            teamComposition = mGuestComposition;
        }

        return teamComposition;
    }

}
