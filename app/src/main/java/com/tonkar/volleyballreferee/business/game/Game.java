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
import java.util.Set;

public abstract class Game implements GameService, TimeoutService, TeamService, Serializable {

    private final     GameType             mGameType;
    private final     long                 mGameDate;
    private final     Rules                mRules;
    private final     Team                 mHomeTeam;
    private final     Team                 mGuestTeam;
    private           TeamType             mTeamOnLeftSide;
    private           TeamType             mTeamOnRightSide;
    private final     List<GameSet>        mSets;
    private           TeamType             mServingTeamAtStart;
    private transient Set<GameListener>    mGameListeners;
    private transient Set<TimeoutListener> mTimeoutListeners;
    private transient Set<TeamListener>    mTeamListeners;

    protected Game(final GameType gameType, final Rules rules) {
        mGameType = gameType;
        mGameDate = System.currentTimeMillis();
        mHomeTeam = createTeam(TeamType.HOME);
        mGuestTeam = createTeam(TeamType.GUEST);
        mTeamOnLeftSide = TeamType.HOME;
        mTeamOnRightSide = TeamType.GUEST;
        mSets = new ArrayList<>();

        mRules = rules;
        mServingTeamAtStart = TeamType.HOME;

        mSets.add(new GameSet(mRules.getPointsPerSet(), mRules.getTeamTimeoutsPerSet(), mServingTeamAtStart));

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
        GameSet gameSet = mSets.get(setIndex);

        if (gameSet != null) {
            count = gameSet.getPoints(teamType);
        }

        return count;
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        List<TeamType> pointsLadder = new ArrayList<>();
        GameSet gameSet = mSets.get(setIndex);

        if (gameSet != null) {
            pointsLadder = gameSet.getPointsLadder();
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

        if (currentSet().isSetComplete()) {
            completeCurrentSet();
        }
    }

    @Override
    public void removePoint(final TeamType teamType) {
        final int oldCount = currentSet().getPoints(teamType);

        if (oldCount > 0) {
            final TeamType oldServingTeam = currentSet().getServingTeam();

            final int newCount = currentSet().removePoint(teamType);
            notifyPointsUpdated(teamType, newCount);

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

        for (GameSet set : mSets) {
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

    // Sets

    GameSet currentSet() {
        return mSets.get(mSets.size() - 1);
    }

    @Override
    public int getNumberOfSets() {
        return mSets.size();
    }

    @Override
    public int getSets(TeamType teamType) {
        int setCount = 0;

        for (final GameSet set : mSets) {
            if (set.isSetComplete() && set.getLeadingTeam().equals(teamType)) {
                setCount++;
            }
        }

        return setCount;
    }

    @Override
    public long getSetDuration() {
        long duration = 0L;
        GameSet gameSet = currentSet();

        if (gameSet != null) {
            duration = gameSet.getDuration();
        }

        return duration;
    }

    @Override
    public long getSetDuration(int setIndex) {
        long duration = 0L;
        GameSet gameSet = mSets.get(setIndex);

        if (gameSet != null) {
            duration = gameSet.getDuration();
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
            mSets.add(new GameSet(pointsToWinSet, mRules.getTeamTimeoutsPerSet(), mServingTeamAtStart));
            // Both coaches must provide a team composition to the referee for each new set
            putAllPlayersOnBench();
            // Both teams change sides between sets
            swapTeams(ActionOriginType.APPLICATION);
            // The service goes to the other team
            swapServiceAtStart();
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

    private Team getTeam(final TeamType teamType) {
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
        return getTeam(teamType).getColorId();
    }

    @Override
    public void setTeamColor(TeamType teamType, int colorId) {
        getTeam(teamType).setColorId(colorId);
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
    public int getNumberOfPlayers(TeamType teamType) {
        return getTeam(teamType).getNumberOfPlayers();
    }

    @Override
    public List<Integer> getPlayersOnBench(TeamType teamType) {
        return getTeam(teamType).getPlayersOnBench();
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
    public void substitutePlayer(TeamType teamType, int number, PositionType positionType) {
        if (getTeam(teamType).substitutePlayer(number, positionType)) {
            notifyPlayerChanged(teamType, number, positionType);
        }
    }

    private void putAllPlayersOnBench() {
        putAllPlayersOnBench(TeamType.HOME);
        putAllPlayersOnBench(TeamType.GUEST);
    }

    private void putAllPlayersOnBench(final TeamType teamType) {
        Log.i("VBR-Team", String.format("Put all players of %s team on bench", teamType.toString()));
        for (Integer number : getTeam(teamType).getPlayersOnCourt()) {
            substitutePlayer(teamType, number, PositionType.BENCH);
        }
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

    private void rotateToNextPositions(TeamType teamType) {
        Log.i("VBR-Team", String.format("Rotate all players of %s team to next position", teamType.toString()));
        getTeam(teamType).rotateToNextPositions();

        for (Integer number : getPlayersOnCourt(teamType)) {
            notifyPlayerChanged(teamType, number, getPlayerPosition(teamType, number));
        }

        notifyTeamRotated(teamType);
    }

    private void rotateToPreviousPositions(TeamType teamType) {
        Log.i("VBR-Team", String.format("Rotate all players of %s team to previous position", teamType.toString()));
        getTeam(teamType).rotateToPreviousPositions();

        for (Integer number : getPlayersOnCourt(teamType)) {
            notifyPlayerChanged(teamType, number, getPlayerPosition(teamType, number));
        }

        notifyTeamRotated(teamType);
    }

    private void notifyPlayerChanged(TeamType teamType, int number, PositionType positionType) {
        Log.i("VBR-Team", String.format("Player #%d of %s team is on %s position", number, teamType.toString(), positionType.toString()));
        for (final TeamListener listener : mTeamListeners) {
            listener.onPlayerChanged(teamType, number, positionType);
        }
    }

    private void notifyTeamsSwapped(final TeamType leftTeamType, final TeamType rightTeamType, final ActionOriginType actionOriginType) {
        Log.i("VBR-Team", String.format("Changed sides: %s team is on left, %s team is on right", leftTeamType.toString(), rightTeamType.toString()));
        for (final TeamListener listener : mTeamListeners) {
            listener.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);
        }
    }

    private void notifyTeamRotated(TeamType teamType) {
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
            notifyTimeoutCalled();
        }
    }

    void notifyTechnicalTimeoutReached() {
        Log.i("VBR-Timeout", "Technical timeout is reached");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTimeout(mRules.getTechnicalTimeoutDuration());
        }
    }

    private void notifyTimeoutCalled() {
        Log.i("VBR-Timeout", "Team timeout is called");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTimeout(mRules.getTeamTimeoutDuration());
        }
    }

    private void notifyTimeoutUpdated(final TeamType teamType, final int maxCount, final int newCount) {
        Log.i("VBR-Timeout", String.format("%s has %d timeouts left on %d", teamType.toString(), newCount, maxCount));
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTimeoutUpdated(teamType, maxCount, newCount);
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
