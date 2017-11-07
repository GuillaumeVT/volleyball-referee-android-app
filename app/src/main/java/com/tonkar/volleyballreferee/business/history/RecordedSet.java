package com.tonkar.volleyballreferee.business.history;

import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.List;

public class RecordedSet {

    private final long           mDuration;
    private final int            mHomeTeamPoints;
    private final int            mGuestTeamPoints;
    private final List<TeamType> mPointsLadder;

    public RecordedSet(long duration, int homeTeamPoints, int guestTeamPoints, List<TeamType> pointsLadder) {
        mDuration = duration;
        mHomeTeamPoints = homeTeamPoints;
        mGuestTeamPoints = guestTeamPoints;
        mPointsLadder = pointsLadder;
    }

    long getDuration() {
        return mDuration;
    }

    int getHomeTeamPoints() {
        return mHomeTeamPoints;
    }

    int getGuestTeamPoints() {
        return mGuestTeamPoints;
    }

    List<TeamType> getPointsLadder() {
        return mPointsLadder;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedSet) {
            RecordedSet other = (RecordedSet) obj;
            result = (this.getDuration() == other.getDuration())
                    && (this.getHomeTeamPoints() == other.getHomeTeamPoints())
                    && (this.getGuestTeamPoints() == other.getGuestTeamPoints())
                    && this.getPointsLadder().equals(other.getPointsLadder());
        }

        return result;
    }
}
