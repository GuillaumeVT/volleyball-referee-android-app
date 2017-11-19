package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameListener;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.TimeoutService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.business.team.Team;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public abstract class Game implements GameService, TimeoutService, TeamService, Serializable {

    private final     GameType                       mGameType;
    private final     long                           mGameDate;
    private final     Rules                          mRules;
    private final     Team                           mHomeTeam;
    private final     Team                           mGuestTeam;
    private           TeamType                       mTeamOnLeftSide;
    private           TeamType                       mTeamOnRightSide;
    private final     List<Set>                      mSets;
    private           TeamType                       mServingTeamAtStart;
    private transient java.util.Set<GameListener>    mGameListeners;
    private transient java.util.Set<TimeoutListener> mTimeoutListeners;
    private transient java.util.Set<TeamListener>    mTeamListeners;

    protected Game(final GameType gameType, final Rules rules) {
        mGameType = gameType;
        mRules = rules;
        mGameDate = System.currentTimeMillis();
        mHomeTeam = createTeam(TeamType.HOME);
        mGuestTeam = createTeam(TeamType.GUEST);
        mTeamOnLeftSide = TeamType.HOME;
        mTeamOnRightSide = TeamType.GUEST;
        mSets = new ArrayList<>();

        mServingTeamAtStart = TeamType.HOME;

        mSets.add(new Set(mRules.getPointsPerSet(), mRules.getTeamTimeoutsPerSet(), mServingTeamAtStart));

        initTransientFields();
    }

    protected abstract Team createTeam(TeamType teamType);

    @Override
    public void addGameListener(final GameListener listener) {
        mGameListeners.add(listener);
    }

    @Override
    public void removeGameListener(final GameListener listener) {
        mGameListeners.remove(listener);
    }

    @Override
    public void addTimeoutListener(final TimeoutListener listener) {
        mTimeoutListeners.add(listener);
    }

    @Override
    public void removeTimeoutListener(final TimeoutListener listener) {
        mTimeoutListeners.remove(listener);
    }

    @Override
    public void addTeamListener(final TeamListener listener) {
        mTeamListeners.add(listener);
    }

    @Override
    public void removeTeamListener(final TeamListener listener) {
        mTeamListeners.remove(listener);
    }

    // Points

    @Override
    public int getPoints(TeamType teamType) {
        return currentSet().getPoints(teamType);
    }

    @Override
    public List<TeamType> getPointsLadder() {
        return currentSet().getPointsLadder();
    }

    @Override
    public int getPoints(TeamType teamType, int setIndex) {
        int count = 0;
        Set set = mSets.get(setIndex);

        if (set != null) {
            count = set.getPoints(teamType);
        }

        return count;
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        List<TeamType> pointsLadder = new ArrayList<>();
        Set set = mSets.get(setIndex);

        if (set != null) {
            pointsLadder = set.getPointsLadder();
        }

        return pointsLadder;
    }

    @Override
    public void addPoint(final TeamType teamType) {
        final TeamType oldServingTeam = currentSet().getServingTeam();

        final int newCount = currentSet().addPoint(teamType);
        notifyPointsUpdated(teamType, newCount);

        final TeamType newServingTeam = currentSet().getServingTeam();

        // In volley, the team obtaining the service rotates to next positions
        if (!oldServingTeam.equals(newServingTeam)) {
            notifyServiceSwapped(currentSet().getServingTeam());
            rotateToNextPositions(newServingTeam);
        }

        if (currentSet().isSetCompleted()) {
            completeCurrentSet();
        }
    }

    @Override
    public void removeLastPoint() {
        final TeamType oldServingTeam = currentSet().getServingTeam();
        final TeamType teamLosingOnePoint = currentSet().removeLastPoint();

        if (teamLosingOnePoint != null) {
            final int newCount = currentSet().getPoints(teamLosingOnePoint);
            notifyPointsUpdated(teamLosingOnePoint, newCount);

            final TeamType newServingTeam = currentSet().getServingTeam();

            if (!oldServingTeam.equals(newServingTeam)) {
                notifyServiceSwapped(currentSet().getServingTeam());
                rotateToPreviousPositions(oldServingTeam);
            }
        }
    }

    private void notifyPointsUpdated(final TeamType teamType, int newCount) {
        Log.i("VBR-Score", String.format("Points are updated for %s team: %d", teamType.toString(), newCount));

        for (final GameListener listener : mGameListeners) {
            listener.onPointsUpdated(teamType, newCount);
        }
    }

    // Game

    @Override
    public Rules getRules() {
        return mRules;
    }

    @Override
    public GameType getGameType() {
        return mGameType;
    }

    @Override
    public long getGameDate() {
        return mGameDate;
    }

    @Override
    public String getGameSummary() {
        StringBuilder builder = new StringBuilder(String.format(Locale.getDefault(),"%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), getSets(TeamType.HOME),getSets(TeamType.GUEST), mGuestTeam.getName()));

        for (Set set : mSets) {
            builder.append(set.getSetSummary());
            if (mSets.indexOf(set) != mSets.size() - 1) {
                builder.append("\t\t");
            }
        }

        return builder.toString();
    }

    @Override
    public boolean isGameCompleted() {
        final int homeTeamSetCount = getSets(TeamType.HOME);
        final int guestTeamSetCount = getSets(TeamType.GUEST);

        // Game is complete when a team reaches the number of sets to win (e.g. 3, 2, 1)
        return (homeTeamSetCount > 0 && homeTeamSetCount * 2 > mRules.getSetsPerGame())
                || (guestTeamSetCount > 0 && guestTeamSetCount * 2 > mRules.getSetsPerGame());
    }

    private void notifyGameCompleted(final TeamType winner) {
        Log.i("VBR-Score", String.format("Game is completed and %s team won", winner.toString()));

        for (final GameListener listener : mGameListeners) {
            listener.onGameCompleted(winner);
        }
    }

    @Override
    public boolean isGamePoint() {
        final int homeTeamSetCount = 1 + getSets(TeamType.HOME);
        final int guestTeamSetCount = 1 + getSets(TeamType.GUEST);

        return !isGameCompleted() && isSetPoint()
                && ((TeamType.HOME.equals(getLeadingTeam()) && homeTeamSetCount * 2 > mRules.getSetsPerGame()) || (TeamType.GUEST.equals(getLeadingTeam()) && guestTeamSetCount * 2 > mRules.getSetsPerGame()));
    }

    @Override
    public boolean isSetPoint() {
        return currentSet().isSetPoint();
    }

    @Override
    public TeamType getLeadingTeam() {
        return currentSet().getLeadingTeam();
    }

    // Sets

    Set currentSet() {
        return mSets.get(mSets.size() - 1);
    }

    @Override
    public int getNumberOfSets() {
        return mSets.size();
    }

    @Override
    public int getSets(TeamType teamType) {
        int setCount = 0;

        for (final Set set : mSets) {
            if (set.isSetCompleted() && set.getLeadingTeam().equals(teamType)) {
                setCount++;
            }
        }

        return setCount;
    }

    @Override
    public long getSetDuration() {
        long duration = 0L;
        Set set = currentSet();

        if (set != null) {
            duration = set.getDuration();
        }

        return duration;
    }

    @Override
    public long getSetDuration(int setIndex) {
        long duration = 0L;
        Set set = mSets.get(setIndex);

        if (set != null) {
            duration = set.getDuration();
        }

        return duration;
    }

    private void completeCurrentSet() {
        notifySetCompleted();

        if (isGameCompleted()) {
            final TeamType winner = getSets(TeamType.HOME) > getSets(TeamType.GUEST) ? TeamType.HOME : TeamType.GUEST;
            notifyGameCompleted(winner);
        } else {
            // The tie break is always played in 15 points
            final int pointsToWinSet = isTieBreakSet() ? 15 : mRules.getPointsPerSet();
            mSets.add(new Set(pointsToWinSet, mRules.getTeamTimeoutsPerSet(), mServingTeamAtStart));
            onNewSet();
            // Both teams change sides between sets
            swapTeams(ActionOriginType.APPLICATION);
            // The service goes to the other team
            swapServiceAtStart();

            if (mRules.areGameIntervalsEnabled()) {
                notifyGameIntervalReached();
            }
        }
    }

    boolean isTieBreakSet() {
        return getSets(TeamType.HOME) + getSets(TeamType.GUEST) + 1 == mRules.getSetsPerGame()
                && mRules.isTieBreakInLastSet()
                && mRules.getSetsPerGame() > 1;
    }

    private void notifySetsUpdated(final TeamType teamType, int newCount) {
        Log.i("VBR-Score", String.format("Sets are updated for %s team: %d", teamType.toString(), newCount));

        for (final GameListener listener : mGameListeners) {
            listener.onSetsUpdated(teamType, newCount);
        }
    }

    private void notifySetCompleted() {
        Log.i("VBR-Score", "Set is completed ");

        notifySetsUpdated(TeamType.HOME, getSets(TeamType.HOME));
        notifySetsUpdated(TeamType.GUEST, getSets(TeamType.GUEST));

        for (final GameListener listener : mGameListeners) {
            listener.onSetCompleted();
        }
    }

    // Service

    @Override
    public TeamType getServingTeam() {
        return currentSet().getServingTeam();
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

            currentSet().setServingTeamAtStart(mServingTeamAtStart);
            notifyServiceSwapped(currentSet().getServingTeam());
        }
    }

    private void notifyServiceSwapped(final TeamType servingTeam) {
        Log.i("VBR-Score", String.format("%s team is now serving", servingTeam.toString()));

        for (final GameListener listener : mGameListeners) {
            listener.onServiceSwapped(servingTeam);
        }
    }

    // Team

    Team getTeam(final TeamType teamType) {
        Team team;

        if (TeamType.HOME.equals(teamType)) {
            team = mHomeTeam;
        } else {
            team = mGuestTeam;
        }

        return team;
    }

    @Override
    public String getTeamName(TeamType teamType) {
        return getTeam(teamType).getName();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {
        getTeam(teamType).setName(name);
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return getTeam(teamType).getColor();
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        getTeam(teamType).setColor(color);
    }

    @Override
    public void addPlayer(TeamType teamType, int number) {
        getTeam(teamType).addPlayer(number);
    }

    @Override
    public void removePlayer(TeamType teamType, int number) {
        getTeam(teamType).removePlayer(number);
    }

    @Override
    public boolean hasPlayer(TeamType teamType, int number) {
        return getTeam(teamType).hasPlayer(number);
    }

    @Override
    public List<Integer> getPlayers(TeamType teamType) {
        return getTeam(teamType).getPlayers();
    }

    @Override
    public List<Integer> getPlayersOnCourt(TeamType teamType) {
        return getTeam(teamType).getPlayersOnCourt();
    }

    @Override
    public PositionType getPlayerPosition(TeamType teamType, int number) {
        PositionType positionType = null;

        if (hasPlayer(teamType, number)) {
            positionType = getTeam(teamType).getPlayerPosition(number);
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPosition(TeamType teamType, PositionType positionType) {
        return getTeam(teamType).getPlayerAtPosition(positionType);
    }

    protected abstract void onNewSet();

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

    private void rotateToNextPositions(TeamType teamType) {
        Log.i("VBR-Team", String.format("Rotate all players of %s team to next position", teamType.toString()));
        getTeam(teamType).rotateToNextPositions();
        notifyTeamRotated(teamType);
    }

    private void rotateToPreviousPositions(TeamType teamType) {
        Log.i("VBR-Team", String.format("Rotate all players of %s team to previous position", teamType.toString()));
        getTeam(teamType).rotateToPreviousPositions();
        notifyTeamRotated(teamType);
    }

    void notifyPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        Log.i("VBR-Team", String.format("Player #%d of %s team is on %s position", number, teamType.toString(), positionType.toString()));
        for (final TeamListener listener : mTeamListeners) {
            listener.onPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    private void notifyTeamsSwapped(final TeamType leftTeamType, final TeamType rightTeamType, final ActionOriginType actionOriginType) {
        Log.i("VBR-Team", String.format("Changed sides: %s team is on left, %s team is on right", leftTeamType.toString(), rightTeamType.toString()));
        for (final TeamListener listener : mTeamListeners) {
            listener.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);
        }
    }

    void notifyTeamRotated(TeamType teamType) {
        Log.i("VBR-Team", String.format("%s team rotated", teamType.toString()));
        for (final TeamListener listener : mTeamListeners) {
            listener.onTeamRotated(teamType);
        }
    }

    // Timeout

    @Override
    public int getTimeouts(TeamType teamType) {
        return currentSet().getTimeouts(teamType);
    }

    @Override
    public void callTimeout(final TeamType teamType) {
        final int oldCount = currentSet().getTimeouts(teamType);

        if (mRules.areTeamTimeoutsEnabled() && oldCount > 0) {
            final int newCount = currentSet().removeTimeout(teamType);

            notifyTimeoutUpdated(teamType, mRules.getTeamTimeoutsPerSet(), newCount);
            notifyTimeoutCalled(teamType);
        }
    }

    private void notifyTimeoutCalled(TeamType teamType) {
        Log.i("VBR-Timeout", "Team timeout is called");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTimeout(teamType, mRules.getTeamTimeoutDuration());
        }
    }

    private void notifyTimeoutUpdated(final TeamType teamType, final int maxCount, final int newCount) {
        Log.i("VBR-Timeout", String.format("%s has %d timeouts left on %d", teamType.toString(), newCount, maxCount));
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTimeoutUpdated(teamType, maxCount, newCount);
        }
    }

    void notifyTechnicalTimeoutReached() {
        Log.i("VBR-Timeout", "Technical timeout is reached");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTechnicalTimeout(mRules.getTechnicalTimeoutDuration());
        }
    }

    private void notifyGameIntervalReached() {
        Log.i("VBR-Timeout", "Game interval is reached");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onGameInterval(mRules.getGameIntervalDuration());
        }
    }

    private void initTransientFields() {
        mGameListeners = new HashSet<>();
        mTimeoutListeners = new HashSet<>();
        mTeamListeners = new HashSet<>();
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        initTransientFields();
    }

}
