package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.Timeout;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.rules.Rules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class TimeBasedGame implements TimeBasedGameService, Serializable {

    private final     long                         mGameDate;
    private           GenderType                   mGenderType;
    private           String                       mLeagueName;
    private final     TeamDefinition               mHomeTeam;
    private final     TeamDefinition               mGuestTeam;
    private           TeamType                     mTeamOnLeftSide;
    private           TeamType                     mTeamOnRightSide;
    private           int                          mHomeTeamPoints;
    private           int                          mGuestTeamPoints;
    private final     List<TeamType>               mPointsLadder;
    private           long                         mDuration;
    private           long                         mStartTime;
    private           long                         mEndTime;
    private           boolean                      mIsStopped;
    private           TeamType                     mServingTeamAtStart;
    private transient java.util.Set<ScoreListener> mScoreListeners;
    private transient java.util.Set<TeamListener>  mTeamListeners;

    TimeBasedGame() {
        mGameDate = System.currentTimeMillis();
        mGenderType = GenderType.MIXED;
        mLeagueName = "";
        mHomeTeam = new TeamDefinition(TeamType.HOME) {};
        mGuestTeam = new TeamDefinition(TeamType.GUEST) {};
        mTeamOnLeftSide = TeamType.HOME;
        mTeamOnRightSide = TeamType.GUEST;

        mHomeTeamPoints = 0;
        mGuestTeamPoints = 0;
        mPointsLadder = new ArrayList<>();
        mServingTeamAtStart = TeamType.HOME;

        mDuration = 1200000L;
        mStartTime = 0L;
        mEndTime = 0L;
        mIsStopped = false;

        initTransientFields();
    }

    @Override
    public void addScoreListener(final ScoreListener listener) {
        mScoreListeners.add(listener);
    }

    @Override
    public void removeScoreListener(final ScoreListener listener) {
        mScoreListeners.remove(listener);
    }

    @Override
    public void addTimeoutListener(final TimeoutListener listener) {}

    @Override
    public void removeTimeoutListener(final TimeoutListener listener) {}

    @Override
    public void addTeamListener(final TeamListener listener) {
        mTeamListeners.add(listener);
    }

    @Override
    public void removeTeamListener(final TeamListener listener) {
        mTeamListeners.remove(listener);
    }

    private TeamDefinition getTeamDefinition(final TeamType teamType) {
        TeamDefinition teamDefinition;

        if (TeamType.HOME.equals(teamType)) {
            teamDefinition = mHomeTeam;
        } else {
            teamDefinition = mGuestTeam;
        }

        return teamDefinition;
    }

    @Override
    public String getLeagueName() {
        return mLeagueName;
    }

    @Override
    public void setLeagueName(String name) {
        mLeagueName = name;
    }

    @Override
    public String getTeamName(TeamType teamType) {
        return getTeamDefinition(teamType).getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return getTeamDefinition(teamType).getColor();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {
        getTeamDefinition(teamType).setName(name);
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        getTeamDefinition(teamType).setColor(color);
    }

    @Override
    public void addPlayer(TeamType teamType, int number) {}

    @Override
    public void removePlayer(TeamType teamType, int number) {}

    @Override
    public boolean hasPlayer(TeamType teamType, int number) {
        return false;
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        return 0;
    }

    @Override
    public java.util.Set<Integer> getPlayers(TeamType teamType) {
        return new TreeSet<>();
    }

    @Override
    public GenderType getGenderType() {
        return mGenderType;
    }

    @Override
    public GenderType getGenderType(TeamType teamType) {
        return getTeamDefinition(teamType).getGenderType();
    }

    @Override
    public void setGenderType(GenderType genderType) {
        mGenderType = genderType;
        setGenderType(TeamType.HOME, genderType);
        setGenderType(TeamType.GUEST, genderType);
    }

    @Override
    public void setGenderType(TeamType teamType, GenderType genderType) {
        getTeamDefinition(teamType).setGenderType(genderType);
    }

    @Override
    public void initTeams() {
        GenderType homeGender = getGenderType(TeamType.HOME);
        GenderType guestGender = getGenderType(TeamType.GUEST);

        if (homeGender.equals(guestGender)) {
            mGenderType = homeGender;
        } else {
            mGenderType = GenderType.MIXED;
        }
    }

    @Override
    public void callTimeout(TeamType teamType) {}

    @Override
    public int getRemainingTimeouts(TeamType teamType) {
        return 0;
    }

    @Override
    public int getRemainingTimeouts(TeamType teamType, int setIndex) {
        return 0;
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType) {
        return new ArrayList<>();
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        return new ArrayList<>();
    }

    @Override
    public GameType getGameType() {
        return GameType.INDOOR;
    }

    @Override
    public long getGameDate() {
        return mGameDate;
    }

    @Override
    public String getGameSummary() {
        return String.format(Locale.getDefault(),"%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), mHomeTeamPoints, mGuestTeamPoints, mGuestTeam.getName());
    }

    @Override
    public int getNumberOfSets() {
        return 1;
    }

    @Override
    public int getSets(TeamType teamType) {
        int sets = 0;

        if (getPoints(teamType) > getPoints(teamType.other())) {
            sets = 1;
        }

        return sets;
    }

    @Override
    public long getSetDuration(int setIndex) {
        long setDuration = 0L;

        if (isMatchStarted()) {
            setDuration = System.currentTimeMillis() - mStartTime;
        }

        return setDuration;
    }

    @Override
    public int getPoints(TeamType teamType) {
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

    @Override
    public int getPoints(TeamType teamType, int setIndex) {
        return getPoints(teamType);
    }

    @Override
    public List<TeamType> getPointsLadder() {
        return new ArrayList<>(mPointsLadder);
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        return getPointsLadder();
    }

    @Override
    public TeamType getServingTeam() {
        final TeamType servingTeam;

        if (mPointsLadder.isEmpty()) {
            servingTeam = mServingTeamAtStart;
        } else {
            servingTeam = mPointsLadder.get(mPointsLadder.size() - 1);
        }

        return servingTeam;
    }

    @Override
    public TeamType getServingTeam(int setIndex) {
        return getServingTeam();
    }

    @Override
    public boolean isMatchCompleted() {
        return isMatchStarted() && isMatchStopped();
    }

    @Override
    public java.util.Set<Integer> getPlayersOnCourt(TeamType teamType) {
        return new TreeSet<>();
    }

    @Override
    public java.util.Set<Integer> getPlayersOnCourt(TeamType teamType, int setIndex) {
        return getPlayersOnCourt(teamType);
    }

    @Override
    public PositionType getPlayerPosition(TeamType teamType, int number) {
        return PositionType.BENCH;
    }

    @Override
    public PositionType getPlayerPosition(TeamType teamType, int number, int setIndex) {
        return getPlayerPosition(teamType, number);
    }

    @Override
    public int getPlayerAtPosition(TeamType teamType, PositionType positionType) {
        return -1;
    }

    @Override
    public int getPlayerAtPosition(TeamType teamType, PositionType positionType, int setIndex) {
        return getPlayerAtPosition(teamType, positionType);
    }

    @Override
    public void swapTeams(ActionOriginType actionOriginType) {
        final TeamType tmpSide = mTeamOnLeftSide;
        mTeamOnLeftSide = mTeamOnRightSide;
        mTeamOnRightSide = tmpSide;

        notifyTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, actionOriginType);
    }

    @Override
    public TeamType getTeamOnLeftSide() {
        return mTeamOnLeftSide;
    }

    @Override
    public TeamType getTeamOnRightSide() {
        return mTeamOnRightSide;
    }

    private void notifyTeamsSwapped(final TeamType leftTeamType, final TeamType rightTeamType, final ActionOriginType actionOriginType) {
        Log.i("VBR-Team", String.format("Changed sides: %s team is on left, %s team is on right", leftTeamType.toString(), rightTeamType.toString()));
        for (final TeamListener listener : mTeamListeners) {
            listener.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);
        }
    }

    @Override
    public UsageType getUsageType() {
        return UsageType.TIME_SCOREBOARD;
    }

    @Override
    public void setUsageType(UsageType usageType) {}

    @Override
    public Rules getRules() {
        return null;
    }

    @Override
    public boolean isMatchPoint() {
        return false;
    }

    @Override
    public boolean isSetPoint() {
        return false;
    }

    @Override
    public void addPoint(TeamType teamType) {
        final TeamType oldServingTeam = getServingTeam();
        int newCount = 0;

        switch (teamType) {
            case HOME:
                mHomeTeamPoints++;
                newCount = mHomeTeamPoints;
                break;
            case GUEST:
                mGuestTeamPoints++;
                newCount = mGuestTeamPoints;
                break;
        }

        mPointsLadder.add(teamType);

        notifyPointsUpdated(teamType, newCount);

        final TeamType newServingTeam = getServingTeam();

        if (!oldServingTeam.equals(newServingTeam)) {
            notifyServiceSwapped(newServingTeam);
        }
    }

    @Override
    public void removeLastPoint() {
        final TeamType oldServingTeam = getServingTeam();
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

        if (teamLosingOnePoint != null) {
            final int newCount = getPoints(teamLosingOnePoint);
            notifyPointsUpdated(teamLosingOnePoint, newCount);

            final TeamType newServingTeam = getServingTeam();

            if (!oldServingTeam.equals(newServingTeam)) {
                notifyServiceSwapped(newServingTeam);
            }
        }
    }

    @Override
    public TeamType getLeadingTeam() {
        return mGuestTeamPoints > mHomeTeamPoints ? TeamType.GUEST : TeamType.HOME;
    }

    @Override
    public void swapServiceAtStart() {
        if (getPointsLadder().isEmpty()) {
            switch (mServingTeamAtStart) {
                case HOME:
                    mServingTeamAtStart = TeamType.GUEST;
                    break;
                case GUEST:
                    mServingTeamAtStart = TeamType.HOME;
                    break;
            }

            notifyServiceSwapped(mServingTeamAtStart);
        }
    }

    private void notifyPointsUpdated(final TeamType teamType, int newCount) {
        Log.i("VBR-Score", String.format("Points are updated for %s team: %d", teamType.toString(), newCount));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onPointsUpdated(teamType, newCount);
        }
    }

    private void notifyServiceSwapped(final TeamType servingTeam) {
        Log.i("VBR-Score", String.format("%s team is now serving", servingTeam.toString()));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onServiceSwapped(servingTeam);
        }
    }

    private void notifyMatchCompleted(TeamType winner) {
        Log.i("VBR-Score", String.format("Match is completed and %s team won", winner.toString()));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onSetCompleted();
        }

        for (final ScoreListener listener : mScoreListeners) {
            listener.onMatchCompleted(winner);
        }
    }

    private void initTransientFields() {
        mScoreListeners = new HashSet<>();
        mTeamListeners = new HashSet<>();
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        initTransientFields();
    }

    @Override
    public long getRemainingTime() {
        long remainingTime = mDuration;

        if (isMatchRunning()) {
            remainingTime = mEndTime - System.currentTimeMillis();
        } else if (isMatchStopped()) {
            remainingTime = 0L;
        }

        return remainingTime;
    }

    @Override
    public long getRemainingTime(int setIndex) {
        return getRemainingTime();
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public void setDuration(long duration) {
        mDuration = duration;
    }

    @Override
    public void start() {
        mStartTime = System.currentTimeMillis();
        mEndTime = mStartTime + mDuration;
    }

    @Override
    public void stop() {
        if (isMatchRunning()) {
            mIsStopped = true;
            final TeamType winner = getPoints(TeamType.HOME) > getPoints(TeamType.GUEST) ? TeamType.HOME : TeamType.GUEST;
            notifyMatchCompleted(winner);
        }
    }

    @Override
    public boolean isMatchStarted() {
        return mStartTime > 0L;
    }

    @Override
    public boolean isMatchRunning() {
        return isMatchStarted() && !isMatchStopped();
    }

    @Override
    public boolean isMatchStopped() {
        return mIsStopped;
    }
}