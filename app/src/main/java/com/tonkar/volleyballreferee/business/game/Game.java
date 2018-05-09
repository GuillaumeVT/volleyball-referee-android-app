package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public abstract class Game extends BaseGame {

    @SerializedName("userId")
    private final UserId         mUserId;
    @SerializedName("usageType")
    private       UsageType      mUsageType;
    @SerializedName("gameType")
    private final GameType       mGameType;
    @SerializedName("gameDate")
    private       long           mGameDate;
    @SerializedName("gameSchedule")
    private       long           mGameSchedule;
    @SerializedName("genderType")
    private       GenderType     mGenderType;
    @SerializedName("rules")
    private       Rules          mRules;
    @SerializedName("gameStatus")
    private       GameStatus     mGameStatus;
    @SerializedName("referee")
    private       String         mRefereeName;
    @SerializedName("leagueName")
    private       String         mLeagueName;
    @SerializedName("homeTeam")
    private final TeamDefinition mHomeTeam;
    @SerializedName("guestTeam")
    private final TeamDefinition mGuestTeam;
    @SerializedName("teamOnLeftSide")
    private       TeamType       mTeamOnLeftSide;
    @SerializedName("teamOnRightSide")
    private       TeamType       mTeamOnRightSide;
    @SerializedName("sets")
    private final List<Set>      mSets;
    @SerializedName("servingTeamAtStart")
    private       TeamType       mServingTeamAtStart;
    @SerializedName("homeTeamCards")
    private final List<Sanction> mHomeTeamSanctions;
    @SerializedName("guestTeamCards")
    private final List<Sanction> mGuestTeamSanctions;

    private transient boolean mEnableNotifications;

    private transient java.util.Set<ScoreListener>    mScoreListeners;
    private transient java.util.Set<TimeoutListener>  mTimeoutListeners;
    private transient java.util.Set<TeamListener>     mTeamListeners;
    private transient java.util.Set<SanctionListener> mSanctionListeners;

    protected Game(final GameType gameType, final String refereeName, final UserId userId) {
        super();
        mUserId = userId;
        mUsageType = UsageType.NORMAL;
        mGameType = gameType;
        mGenderType = GenderType.MIXED;
        mGameStatus = GameStatus.SCHEDULED;
        mRefereeName = refereeName;
        mLeagueName = "";
        mHomeTeam = createTeamDefinition(TeamType.HOME);
        mGuestTeam = createTeamDefinition(TeamType.GUEST);
        mTeamOnLeftSide = TeamType.HOME;
        mTeamOnRightSide = TeamType.GUEST;
        mSets = new ArrayList<>();
        mHomeTeamSanctions = new ArrayList<>();
        mGuestTeamSanctions = new ArrayList<>();

        mServingTeamAtStart = TeamType.HOME;

        initTransientFields();
    }

    protected abstract TeamDefinition createTeamDefinition(TeamType teamType);

    protected abstract Set createSet(Rules rules, boolean isTieBreakSet, TeamType servingTeamAtStart, TeamDefinition homeTeamDefinition, TeamDefinition guestTeamDefinition);

    @Override
    public void addScoreListener(final ScoreListener listener) {
        mScoreListeners.add(listener);
    }

    @Override
    public void removeScoreListener(final ScoreListener listener) {
        mScoreListeners.remove(listener);
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

    @Override
    public void addSanctionListener(SanctionListener listener) {
        mSanctionListeners.add(listener);
    }

    @Override
    public void removeSanctionListener(SanctionListener listener) {
        mSanctionListeners.remove(listener);
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
            notifyServiceSwapped(newServingTeam);
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
                notifyServiceSwapped(newServingTeam);
                rotateToPreviousPositions(oldServingTeam);
            }
        }
    }

    private void notifyPointsUpdated(final TeamType teamType, int newCount) {
        Log.i("VBR-Score", String.format("Points are updated for %s team: %d", teamType.toString(), newCount));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onPointsUpdated(teamType, newCount);
        }
    }

    // General

    @Override
    public UserId getUserId() {
        return mUserId;
    }

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
    public long getGameSchedule() {
        return mGameSchedule;
    }

    @Override
    public GameStatus getMatchStatus() {
        return mGameStatus;
    }

    // Score

    @Override
    public String getGameSummary() {
        StringBuilder builder = new StringBuilder(String.format(Locale.getDefault(),"%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), getSets(TeamType.HOME), getSets(TeamType.GUEST), mGuestTeam.getName()));

        for (Set set : mSets) {
            builder.append(set.getSetSummary());
            if (mSets.indexOf(set) != mSets.size() - 1) {
                builder.append("\t\t");
            }
        }

        return builder.toString();
    }

    @Override
    public void startMatch(Rules rules, long gameDate, long gameSchedule) {
        mRules = rules;
        mGameDate = gameDate;
        mGameSchedule = gameSchedule;

        mRules.printRules();

        GenderType homeGender = getGenderType(TeamType.HOME);
        GenderType guestGender = getGenderType(TeamType.GUEST);

        if (homeGender.equals(guestGender)) {
            mGenderType = homeGender;
        } else {
            mGenderType = GenderType.MIXED;
        }

        mSets.add(createSet(mRules, false, mServingTeamAtStart, mHomeTeam, mGuestTeam));
        mGameStatus = GameStatus.LIVE;
    }


    @Override
    public boolean isMatchCompleted() {
        final int homeTeamSetCount = getSets(TeamType.HOME);
        final int guestTeamSetCount = getSets(TeamType.GUEST);

        // Match is complete when a team reaches the number of sets to win (e.g. 3, 2, 1)
        return (homeTeamSetCount > 0 && homeTeamSetCount * 2 > mRules.getSetsPerGame())
                || (guestTeamSetCount > 0 && guestTeamSetCount * 2 > mRules.getSetsPerGame());
    }

    private void notifyMatchCompleted(final TeamType winner) {
        Log.i("VBR-Score", String.format("Match is completed and %s team won", winner.toString()));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onMatchCompleted(winner);
        }
    }

    @Override
    public boolean isMatchPoint() {
        final int homeTeamSetCount = 1 + getSets(TeamType.HOME);
        final int guestTeamSetCount = 1 + getSets(TeamType.GUEST);

        return !isMatchCompleted() && isSetPoint()
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
        return mSets.get(currentSetIndex());
    }

    private int currentSetIndex() {
        return mSets.size() - 1;
    }

    Set getSet(int setIndex) {
        return mSets.get(setIndex);
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

        if (isMatchCompleted()) {
            mGameStatus = GameStatus.COMPLETED;
            final TeamType winner = getSets(TeamType.HOME) > getSets(TeamType.GUEST) ? TeamType.HOME : TeamType.GUEST;
            notifyMatchCompleted(winner);
        } else {
            mSets.add(createSet(mRules, isTieBreakSet(), mServingTeamAtStart, mHomeTeam, mGuestTeam));
            if (isTieBreakSet()) {
                // Before the tie break the toss has to be done
                swapTeams(ActionOriginType.USER);
                swapTeams(ActionOriginType.USER);
            } else {
                // Both teams change sides between sets
                swapTeams(ActionOriginType.APPLICATION);
                // The service goes to the other team
                swapServiceAtStart();
            }

            if (mRules.areGameIntervalsEnabled()) {
                notifyGameIntervalReached();
            }

            notifySetStarted();
        }
    }

    boolean isTieBreakSet() {
        return getSets(TeamType.HOME) + getSets(TeamType.GUEST) + 1 == mRules.getSetsPerGame()
                && mRules.isTieBreakInLastSet()
                && mRules.getSetsPerGame() > 1;
    }

    private void notifySetsUpdated(final TeamType teamType, int newCount) {
        Log.i("VBR-Score", String.format("Sets are updated for %s team: %d", teamType.toString(), newCount));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onSetsUpdated(teamType, newCount);
        }
    }

    private void notifySetStarted() {
        Log.i("VBR-Score", "Set is started");

        for (final ScoreListener listener : mScoreListeners) {
            listener.onSetStarted();
        }
    }

    private void notifySetCompleted() {
        Log.i("VBR-Score", "Set is completed");

        notifySetsUpdated(TeamType.HOME, getSets(TeamType.HOME));
        notifySetsUpdated(TeamType.GUEST, getSets(TeamType.GUEST));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onSetCompleted();
        }
    }

    // Service

    @Override
    public TeamType getServingTeam() {
        return currentSet().getServingTeam();
    }

    @Override
    public TeamType getServingTeam(int setIndex) {
        return getSet(setIndex).getServingTeam();
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

        for (final ScoreListener listener : mScoreListeners) {
            listener.onServiceSwapped(servingTeam);
        }
    }

    // TeamComposition

    TeamDefinition getTeamDefinition(final TeamType teamType) {
        TeamDefinition teamDefinition;

        if (TeamType.HOME.equals(teamType)) {
            teamDefinition = mHomeTeam;
        } else {
            teamDefinition = mGuestTeam;
        }

        return teamDefinition;
    }

    @Override
    public String getRefereeName() {
        return mRefereeName;
    }

    @Override
    public void setRefereeName(String name) {
        mRefereeName = name;
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
    public void setTeamName(TeamType teamType, String name) {
        getTeamDefinition(teamType).setName(name);
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return getTeamDefinition(teamType).getColor();
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        getTeamDefinition(teamType).setColor(color);
    }

    @Override
    public void addPlayer(TeamType teamType, int number) {
        getTeamDefinition(teamType).addPlayer(number);
    }

    @Override
    public void removePlayer(TeamType teamType, int number) {
        getTeamDefinition(teamType).removePlayer(number);
    }

    @Override
    public boolean hasPlayer(TeamType teamType, int number) {
        return getTeamDefinition(teamType).hasPlayer(number);
    }

    @Override
    public java.util.Set<Integer> getPlayers(TeamType teamType) {
        return getTeamDefinition(teamType).getPlayers();
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
    public int getNumberOfPlayers(TeamType teamType) {
        return getTeamDefinition(teamType).getNumberOfPlayers();
    }

    @Override
    public java.util.Set<Integer> getPlayersOnCourt(TeamType teamType) {
        return currentSet().getTeamComposition(teamType).getPlayersOnCourt();
    }

    @Override
    public java.util.Set<Integer> getPlayersOnCourt(TeamType teamType, int setIndex) {
        return getSet(setIndex).getTeamComposition(teamType).getPlayersOnCourt();
    }

    @Override
    public PositionType getPlayerPosition(TeamType teamType, int number) {
        PositionType positionType = null;

        if (hasPlayer(teamType, number)) {
            positionType = currentSet().getTeamComposition(teamType).getPlayerPosition(number);
        }

        return positionType;
    }

    @Override
    public PositionType getPlayerPosition(TeamType teamType, int number, int setIndex) {
        PositionType positionType = null;

        if (hasPlayer(teamType, number)) {
            positionType = getSet(setIndex).getTeamComposition(teamType).getPlayerPosition(number);
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPosition(TeamType teamType, PositionType positionType) {
        return currentSet().getTeamComposition(teamType).getPlayerAtPosition(positionType);
    }

    @Override
    public int getPlayerAtPosition(TeamType teamType, PositionType positionType, int setIndex) {
        return getSet(setIndex).getTeamComposition(teamType).getPlayerAtPosition(positionType);
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

    @Override
    public UsageType getUsageType() {
        return mUsageType;
    }

    void rotateToNextPositions(TeamType teamType) {
        Log.i("VBR-Team", String.format("Rotate all players of %s team to next position", teamType.toString()));
        currentSet().getTeamComposition(teamType).rotateToNextPositions();
        notifyTeamRotated(teamType);
    }

    void rotateToPreviousPositions(TeamType teamType) {
        Log.i("VBR-Team", String.format("Rotate all players of %s team to previous position", teamType.toString()));
        currentSet().getTeamComposition(teamType).rotateToPreviousPositions();
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
    public int getRemainingTimeouts(TeamType teamType) {
        return currentSet().getRemainingTimeouts(teamType);
    }

    @Override
    public int getRemainingTimeouts(TeamType teamType, int setIndex) {
        int timeouts = 0;
        Set set = mSets.get(setIndex);

        if (set != null) {
            timeouts = set.getRemainingTimeouts(teamType);
        }

        return timeouts;
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType) {
        return currentSet().getCalledTimeouts(teamType);
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        List<Timeout> timeouts = new ArrayList<>();
        Set set = mSets.get(setIndex);

        if (set != null) {
            timeouts = set.getCalledTimeouts(teamType);
        }

        return timeouts;
    }

    @Override
    public void callTimeout(final TeamType teamType) {
        final int oldCount = currentSet().getRemainingTimeouts(teamType);

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

    @Override
    public void giveSanction(TeamType teamType, SanctionType sanctionType, int number) {
        Sanction sanction = new Sanction(number, sanctionType, currentSetIndex(), getPoints(TeamType.HOME), getPoints(TeamType.GUEST));
        if (TeamType.HOME.equals(teamType)) {
            mHomeTeamSanctions.add(sanction);
        } else {
            mGuestTeamSanctions.add(sanction);
        }

        if (SanctionType.RED.equals(sanctionType) || SanctionType.DELAY_PENALTY.equals(sanctionType)) {
            addPoint(teamType.other());
        }

        notifySanctionGiven(teamType, sanctionType, number);
    }

    private void notifySanctionGiven(TeamType teamType, SanctionType sanctionType, int number) {
        Log.i("VBR-Card", String.format("Player %d of %s team was given a %s sanction", number, teamType.toString(), sanctionType.toString()));
        for (final SanctionListener listener : mSanctionListeners) {
            listener.onSanction(teamType, sanctionType, number);
        }
    }

    @Override
    public List<Sanction> getGivenSanctions(TeamType teamType) {
        List<Sanction> sanctions;

        if (TeamType.HOME.equals(teamType)) {
            sanctions = new ArrayList<>(mHomeTeamSanctions);
        } else {
            sanctions = new ArrayList<>(mGuestTeamSanctions);
        }

        return sanctions;
    }

    @Override
    public List<Sanction> getGivenSanctions(TeamType teamType, int setIndex) {
        List<Sanction> sanctionsForSet = new ArrayList<>();

        for (Sanction sanction : getGivenSanctions(teamType)) {
            if (sanction.getSetIndex() == setIndex) {
                sanctionsForSet.add(sanction);
            }
        }

        return sanctionsForSet;
    }

    @Override
    public List<Sanction> getSanctions(TeamType teamType, int number) {
        List<Sanction> sanctionsForPlayer = new ArrayList<>();

        for (Sanction sanction : getGivenSanctions(teamType)) {
            if (sanction.getPlayer() == number) {
                sanctionsForPlayer.add(sanction);
            }
        }

        return sanctionsForPlayer;
    }

    @Override
    public boolean hasSanctions(TeamType teamType, int number) {
        return getSanctions(teamType, number).size() > 0;
    }

    @Override
    public java.util.Set<Integer> getExpulsedOrDisqualifiedPlayersForCurrentSet(TeamType teamType) {
        java.util.Set<Integer> players = new HashSet<>();

        List<Sanction> sanctionsForSet = getGivenSanctions(teamType);
        int currentSetIndex = currentSetIndex();

        for (Sanction sanction : sanctionsForSet) {
            if (SanctionType.RED_DISQUALIFICATION.equals(sanction.getSanctionType())) {
                players.add(sanction.getPlayer());
            } else if (SanctionType.RED_EXPULSION.equals(sanction.getSanctionType()) && sanction.getSetIndex() == currentSetIndex) {
                players.add(sanction.getPlayer());
            }
        }

        return players;
    }

    private void initTransientFields() {
        mEnableNotifications = true;
        mScoreListeners = new HashSet<>();
        mTimeoutListeners = new HashSet<>();
        mTeamListeners = new HashSet<>();
        mSanctionListeners = new HashSet<>();
    }

    @Override
    public void setUsageType(UsageType usageType) {
        mUsageType = usageType;
    }

    @Override
    public boolean areNotificationsEnabled() {
        return mEnableNotifications;
    }

    void forceFinishSet(TeamType teamType) {
        mEnableNotifications = false;

        // By using the variable currentSet, we add points until this set is finished
        Set currentSet = currentSet();

        while(!currentSet.isSetCompleted()) {
            addPoint(teamType);
        }

        mEnableNotifications = true;
    }

    void forceFinishMatch(TeamType teamType) {
        mEnableNotifications = false;

        // By using the function currentSet(), we add points until every set is finished (= match)
        while(!currentSet().isSetCompleted()) {
            addPoint(teamType);
        }

        mEnableNotifications = true;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Game) {
            Game other = (Game) obj;
            result = super.equals(other)
                    && (this.getUsageType().equals(other.getUsageType()))
                    && (this.getGameType().equals(other.getGameType()))
                    && (this.getGameDate() == other.getGameDate())
                    && (this.getGenderType().equals(other.getGenderType()))
                    && (this.getRules().equals(other.getRules()))
                    && (this.getLeagueName().equals(other.getLeagueName()))
                    && (this.getTeamDefinition(TeamType.HOME).equals(other.getTeamDefinition(TeamType.HOME)))
                    && (this.getTeamDefinition(TeamType.GUEST).equals(other.getTeamDefinition(TeamType.GUEST)))
                    && (this.getTeamOnLeftSide().equals(other.getTeamOnLeftSide()))
                    && (this.getTeamOnRightSide().equals(other.getTeamOnRightSide()));

            if (result) {
                for (int setIndex = 0; setIndex < getNumberOfSets(); setIndex++) {
                    result = result && this.getSet(setIndex).equals(other.getSet(setIndex));
                }
            }
        }

        return result;
    }
}
