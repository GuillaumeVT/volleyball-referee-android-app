package com.tonkar.volleyballreferee.engine.game;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.sanction.*;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.game.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public abstract class Game extends BaseGame {

    @SerializedName("id")
    private final String                                                 mId;
    @SerializedName("createdBy")
    private final String                                                 mCreatedBy;
    @SerializedName("createdAt")
    private final long                                                   mCreatedAt;
    @SerializedName("updatedAt")
    private       long                                                   mUpdatedAt;
    @SerializedName("scheduledAt")
    private       long                                                   mScheduledAt;
    @SerializedName("refereedBy")
    private       String                                                 mRefereedBy;
    @SerializedName("refereeName")
    private       String                                                 mRefereeName;
    @SerializedName("kind")
    private final GameType                                               mKind;
    @SerializedName("gender")
    private       GenderType                                             mGender;
    @SerializedName("usage")
    private       UsageType                                              mUsage;
    @SerializedName("status")
    private       GameStatus                                             mGameStatus;
    @SerializedName("rules")
    private final Rules                                                  mRules;
    @SerializedName("league")
    private final SelectedLeagueDto                                      mLeague;
    @SerializedName("homeTeam")
    private final TeamDefinition                                         mHomeTeam;
    @SerializedName("guestTeam")
    private final TeamDefinition                                         mGuestTeam;
    @SerializedName("teamOnLeftSide")
    private       TeamType                                               mTeamOnLeftSide;
    @SerializedName("teamOnRightSide")
    private       TeamType                                               mTeamOnRightSide;
    @SerializedName("sets")
    private final List<com.tonkar.volleyballreferee.engine.game.set.Set> mSets;
    @SerializedName("servingTeamAtStart")
    private       TeamType                                               mServingTeamAtStart;
    @SerializedName("homeTeamCards")
    private final List<SanctionDto>                                      mHomeTeamSanctions;
    @SerializedName("guestTeamCards")
    private final List<SanctionDto>                                      mGuestTeamSanctions;
    @SerializedName("startTime")
    private       long                                                   mStartTime;
    @SerializedName("endTime")
    private       long                                                   mEndTime;
    @SerializedName("referee1Name")
    private       String                                                 mReferee1Name;
    @SerializedName("referee2Name")
    private       String                                                 mReferee2Name;
    @SerializedName("scorerName")
    private       String                                                 mScorerName;

    private transient boolean mEnableNotifications;

    private transient Set<ScoreListener>    mScoreListeners;
    private transient Set<TimeoutListener>  mTimeoutListeners;
    private transient Set<TeamListener>     mTeamListeners;
    private transient Set<SanctionListener> mSanctionListeners;

    protected Game(GameType kind, String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        super();
        mId = id;
        mCreatedBy = createdBy;
        mCreatedAt = createdAt;
        mUpdatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        mScheduledAt = scheduledAt;
        mRefereedBy = createdBy;
        mRefereeName = refereeName;
        mKind = kind;
        mGender = GenderType.MIXED;
        mGameStatus = GameStatus.SCHEDULED;
        mUsage = UsageType.NORMAL;
        mRules = rules;
        mLeague = new SelectedLeagueDto();
        mLeague.setId(UUID.randomUUID().toString());
        mLeague.setCreatedBy(createdBy);
        mLeague.setCreatedAt(createdAt);
        mLeague.setUpdatedAt(createdAt);
        mLeague.setKind(kind);
        mLeague.setName("");
        mLeague.setDivision("");
        mHomeTeam = createTeamDefinition(TeamType.HOME);
        mGuestTeam = createTeamDefinition(TeamType.GUEST);
        mTeamOnLeftSide = TeamType.HOME;
        mTeamOnRightSide = TeamType.GUEST;
        mSets = new ArrayList<>();
        mHomeTeamSanctions = new ArrayList<>();
        mGuestTeamSanctions = new ArrayList<>();
        mStartTime = mScheduledAt;
        mEndTime = 0L;
        mReferee1Name = "";
        mReferee2Name = "";
        mScorerName = "";

        mServingTeamAtStart = TeamType.HOME;

        initTransientFields();
    }

    protected abstract TeamDefinition createTeamDefinition(TeamType teamType);

    protected abstract com.tonkar.volleyballreferee.engine.game.set.Set createSet(Rules rules,
                                                                                  int pointsToWinSet,
                                                                                  TeamType servingTeamAtStart);

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

    // General

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getCreatedBy() {
        return mCreatedBy;
    }

    @Override
    public long getCreatedAt() {
        return mCreatedAt;
    }

    @Override
    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    @Override
    public void setUpdatedAt(long updatedAt) {
        mUpdatedAt = updatedAt;
    }

    @Override
    public long getScheduledAt() {
        return mScheduledAt;
    }

    @Override
    public String getRefereedBy() {
        return mRefereedBy;
    }

    @Override
    public void setRefereedBy(String refereedBy) {
        mRefereedBy = refereedBy;
    }

    @Override
    public String getRefereeName() {
        return mRefereeName;
    }

    @Override
    public void setRefereeName(String refereeName) {
        mRefereeName = refereeName;
    }

    @Override
    public GameType getKind() {
        return mKind;
    }

    @Override
    public GameStatus getMatchStatus() {
        return mGameStatus;
    }

    @Override
    public UsageType getUsage() {
        return mUsage;
    }

    @Override
    public void setUsage(UsageType usage) {
        mUsage = usage;
    }

    @Override
    public SelectedLeagueDto getLeague() {
        return mLeague;
    }

    @Override
    public Rules getRules() {
        return mRules;
    }

    @Override
    public String getGameSummary() {
        StringBuilder builder = new StringBuilder(
                String.format(Locale.getDefault(), "%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), getSets(TeamType.HOME),
                              getSets(TeamType.GUEST), mGuestTeam.getName()));

        for (com.tonkar.volleyballreferee.engine.game.set.Set set : mSets) {
            builder.append(set.getSetSummary());
            if (mSets.indexOf(set) != mSets.size() - 1) {
                builder.append("\t\t");
            }
        }

        return builder.toString();
    }

    @Override
    public void startMatch() {
        mRules.printRules();

        if (mScheduledAt == 0L) {
            mScheduledAt = System.currentTimeMillis();
        }

        mStartTime = System.currentTimeMillis();
        mUpdatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();

        mSets.add(createSet(mRules, mRules.getPointsPerSet(), mServingTeamAtStart));
        mGameStatus = GameStatus.LIVE;
    }

    @Override
    public void resetCurrentSet() {
        if (!isMatchCompleted() && currentSetIndex() >= 0) {
            resetCurrentSetSanctions(mHomeTeamSanctions);
            resetCurrentSetSanctions(mGuestTeamSanctions);
            mSets.set(currentSetIndex(),
                      createSet(mRules, isTieBreakSet() ? mRules.getPointsInTieBreak() : mRules.getPointsPerSet(), mServingTeamAtStart));
            notifySetStarted();
        }
    }

    private void resetCurrentSetSanctions(List<SanctionDto> sanctions) {
        sanctions.removeIf(sanction -> sanction.getSet() == currentSetIndex());
    }

    @Override
    public boolean isMatchCompleted() {
        final int homeTeamSetCount = getSets(TeamType.HOME);
        final int guestTeamSetCount = getSets(TeamType.GUEST);

        return switch (mRules.getMatchTermination()) {
            case Rules.ALL_SETS_TERMINATION -> (homeTeamSetCount + guestTeamSetCount == mRules.getSetsPerGame());
            default ->
                // Match is complete when a team reaches the number of sets to win (e.g. 3, 2, 1)
                    (homeTeamSetCount > 0 && homeTeamSetCount * 2 > mRules.getSetsPerGame()) || (guestTeamSetCount > 0 && guestTeamSetCount * 2 > mRules.getSetsPerGame());
        };
    }

    private void notifyMatchCompleted(final TeamType winner) {
        Log.i(Tags.SCORE, String.format("Match is completed and %s team won", winner));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onMatchCompleted(winner);
        }
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
        com.tonkar.volleyballreferee.engine.game.set.Set set = mSets.get(setIndex);

        if (set != null) {
            count = set.getPoints(teamType);
        }

        return count;
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        List<TeamType> pointsLadder = new ArrayList<>();
        com.tonkar.volleyballreferee.engine.game.set.Set set = mSets.get(setIndex);

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
            notifyServiceSwapped(newServingTeam, false);
            rotateToNextPositions(newServingTeam);
        }

        if (currentSet().isSetCompleted()) {
            completeCurrentSet();
        }
    }

    protected void removeLastPoint() {
        final TeamType oldServingTeam = currentSet().getServingTeam();
        final TeamType teamLosingOnePoint = currentSet().removeLastPoint();

        if (teamLosingOnePoint != null) {
            final int newCount = currentSet().getPoints(teamLosingOnePoint);
            notifyPointsUpdated(teamLosingOnePoint, newCount);

            final TeamType newServingTeam = currentSet().getServingTeam();

            if (!oldServingTeam.equals(newServingTeam)) {
                notifyServiceSwapped(newServingTeam, false);
                rotateToPreviousPositions(oldServingTeam);
            }
        }
    }

    private void notifyPointsUpdated(final TeamType teamType, int newCount) {
        Log.i(Tags.SCORE, String.format("Points are updated for %s team: %d", teamType, newCount));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onPointsUpdated(teamType, newCount);
        }
    }

    protected abstract void undoSubstitution(TeamType teamType, SubstitutionDto substitution);

    // Score

    @Override
    public boolean isMatchPoint() {
        boolean matchPoint;

        switch (mRules.getMatchTermination()) {
            case Rules.ALL_SETS_TERMINATION -> matchPoint = !isMatchCompleted() && isSetPoint() && (1 + getSets(TeamType.HOME) + getSets(
                    TeamType.GUEST) == mRules.getSetsPerGame());
            default -> {
                final int homeTeamSetCount = 1 + getSets(TeamType.HOME);
                final int guestTeamSetCount = 1 + getSets(TeamType.GUEST);
                matchPoint = !isMatchCompleted() && isSetPoint() && ((TeamType.HOME.equals(
                        getLeadingTeam()) && homeTeamSetCount * 2 > mRules.getSetsPerGame()) || (TeamType.GUEST.equals(
                        getLeadingTeam()) && guestTeamSetCount * 2 > mRules.getSetsPerGame()));
            }
        }

        return matchPoint;
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

    com.tonkar.volleyballreferee.engine.game.set.Set currentSet() {
        return mSets.get(currentSetIndex());
    }

    int currentSetIndex() {
        return mSets.size() - 1;
    }

    com.tonkar.volleyballreferee.engine.game.set.Set getSet(int setIndex) {
        return mSets.get(setIndex);
    }

    @Override
    public int getNumberOfSets() {
        return mSets.size();
    }

    @Override
    public int getSets(TeamType teamType) {
        int setCount = 0;

        for (final com.tonkar.volleyballreferee.engine.game.set.Set set : mSets) {
            if (set.isSetCompleted() && set.getLeadingTeam().equals(teamType)) {
                setCount++;
            }
        }

        return setCount;
    }

    @Override
    public long getStartTime() {
        return mStartTime;
    }

    @Override
    public long getEndTime() {
        return mEndTime;
    }

    @Override
    public String getReferee1Name() {
        return mReferee1Name;
    }

    @Override
    public void setReferee1Name(String referee1Name) {
        mReferee1Name = referee1Name;
    }

    @Override
    public String getReferee2Name() {
        return mReferee2Name;
    }

    @Override
    public void setReferee2Name(String referee2Name) {
        mReferee2Name = referee2Name;
    }

    @Override
    public String getScorerName() {
        return mScorerName;
    }

    @Override
    public void setScorerName(String scorerName) {
        mScorerName = scorerName;
    }

    @Override
    public long getSetDuration(int setIndex) {
        long duration = 0L;
        com.tonkar.volleyballreferee.engine.game.set.Set set = mSets.get(setIndex);

        if (set != null) {
            duration = set.getDuration();
        }

        return duration;
    }

    @Override
    public long getSetStartTime(int setIndex) {
        return getSet(setIndex).getStartTime();
    }

    @Override
    public long getSetEndTime(int setIndex) {
        return getSet(setIndex).getEndTime();
    }

    private void completeCurrentSet() {
        notifySetCompleted();

        if (isMatchCompleted()) {
            mEndTime = System.currentTimeMillis();
            mGameStatus = GameStatus.COMPLETED;
            final TeamType winner = getSets(TeamType.HOME) > getSets(TeamType.GUEST) ? TeamType.HOME : TeamType.GUEST;
            notifyMatchCompleted(winner);
        } else {
            mSets.add(createSet(mRules, isTieBreakSet() ? mRules.getPointsInTieBreak() : mRules.getPointsPerSet(), mServingTeamAtStart));
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

            if (mRules.isGameIntervals()) {
                notifyGameIntervalReached();
            }

            notifySetStarted();
        }
    }

    boolean isTieBreakSet() {
        return getSets(TeamType.HOME) + getSets(TeamType.GUEST) + 1 == mRules.getSetsPerGame() && mRules.isTieBreakInLastSet() && Arrays
                .asList(3, 5)
                .contains(mRules.getSetsPerGame());
    }

    private void notifySetsUpdated(final TeamType teamType, int newCount) {
        Log.i(Tags.SCORE, String.format("Sets are updated for %s team: %d", teamType, newCount));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onSetsUpdated(teamType, newCount);
        }
    }

    private void notifySetStarted() {
        Log.i(Tags.SCORE, "Set is started");

        for (final ScoreListener listener : mScoreListeners) {
            listener.onSetStarted();
        }
    }

    private void notifySetCompleted() {
        Log.i(Tags.SCORE, "Set is completed");

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
    public TeamType getFirstServingTeam() {
        return currentSet().getServingTeamAtStart();
    }

    @Override
    public TeamType getFirstServingTeam(int setIndex) {
        return getSet(setIndex).getServingTeamAtStart();
    }

    @Override
    public void swapServiceAtStart() {
        if (getPointsLadder().isEmpty()) {
            switch (mServingTeamAtStart) {
                case HOME -> mServingTeamAtStart = TeamType.GUEST;
                case GUEST -> mServingTeamAtStart = TeamType.HOME;
            }

            currentSet().setServingTeamAtStart(mServingTeamAtStart);
            notifyServiceSwapped(currentSet().getServingTeam(), true);
        }
    }

    private void notifyServiceSwapped(final TeamType servingTeam, boolean isStart) {
        Log.i(Tags.SCORE, String.format("%s team is now serving", servingTeam));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onServiceSwapped(servingTeam, isStart);
        }
    }

    // Team definition / composition

    TeamDefinition getTeamDefinition(final TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? mHomeTeam : mGuestTeam;
    }

    @Override
    public String getTeamId(TeamType teamType) {
        return getTeamDefinition(teamType).getId();
    }

    @Override
    public void setTeamId(TeamType teamType, String id) {
        getTeamDefinition(teamType).setId(id);
    }

    @Override
    public String getCreatedBy(TeamType teamType) {
        return getTeamDefinition(teamType).getCreatedBy();
    }

    @Override
    public void setCreatedBy(TeamType teamType, String createdBy) {
        getTeamDefinition(teamType).setCreatedBy(createdBy);
    }

    @Override
    public long getCreatedAt(TeamType teamType) {
        return getTeamDefinition(teamType).getCreatedAt();
    }

    @Override
    public void setCreatedAt(TeamType teamType, long createdAt) {
        getTeamDefinition(teamType).setCreatedAt(createdAt);
    }

    @Override
    public long getUpdatedAt(TeamType teamType) {
        return getTeamDefinition(teamType).getUpdatedAt();
    }

    @Override
    public void setUpdatedAt(TeamType teamType, long updatedAt) {
        getTeamDefinition(teamType).setUpdatedAt(updatedAt);
    }

    @Override
    public GameType getTeamsKind() {
        return mKind;
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
        return getTeamDefinition(teamType).getColorInt();
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        getTeamDefinition(teamType).setColorInt(color);
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
    public Set<PlayerDto> getPlayers(TeamType teamType) {
        return new TreeSet<>(getTeamDefinition(teamType).getPlayers());
    }

    @Override
    public void setPlayerName(TeamType teamType, int number, String name) {
        getTeamDefinition(teamType).setPlayerName(number, name);
    }

    @Override
    public String getPlayerName(TeamType teamType, int number) {
        return getTeamDefinition(teamType).getPlayerName(number);
    }

    @Override
    public GenderType getGender() {
        return mGender;
    }

    @Override
    public GenderType getGender(TeamType teamType) {
        return getTeamDefinition(teamType).getGender();
    }

    @Override
    public void setGender(GenderType gender) {
        mGender = gender;
        setGender(TeamType.HOME, gender);
        setGender(TeamType.GUEST, gender);
    }

    @Override
    public void setGender(TeamType teamType, GenderType gender) {
        getTeamDefinition(teamType).setGender(gender);

        GenderType homeGender = getGender(TeamType.HOME);
        GenderType guestGender = getGender(TeamType.GUEST);

        if (homeGender.equals(guestGender)) {
            mGender = homeGender;
        } else {
            mGender = GenderType.MIXED;
        }
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        return getTeamDefinition(teamType).getNumberOfPlayers();
    }

    @Override
    public void setCaptain(TeamType teamType, int number) {
        getTeamDefinition(teamType).setCaptain(number);
    }

    @Override
    public int getCaptain(TeamType teamType) {
        return getTeamDefinition(teamType).getCaptain();
    }

    @Override
    public Set<Integer> getPossibleCaptains(TeamType teamType) {
        return getTeamDefinition(teamType).getPossibleCaptains();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return getTeamDefinition(teamType).isCaptain(number);
    }

    @Override
    public String getCoachName(TeamType teamType) {
        return getTeamDefinition(teamType).getCoach();
    }

    @Override
    public void setCoachName(TeamType teamType, String name) {
        getTeamDefinition(teamType).setCoach(name);
    }

    @Override
    public Set<Integer> getPlayersOnCourt(TeamType teamType) {
        return currentSet().getTeamComposition(teamType).getPlayersOnCourt();
    }

    @Override
    public Set<Integer> getPlayersOnCourt(TeamType teamType, int setIndex) {
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

    void rotateToNextPositions(TeamType teamType) {
        Log.i(Tags.TEAM, String.format("Rotate all players of %s team to next position", teamType));
        currentSet().getTeamComposition(teamType).rotateToNextPositions();
        notifyTeamRotated(teamType, true);
    }

    void rotateToPreviousPositions(TeamType teamType) {
        Log.i(Tags.TEAM, String.format("Rotate all players of %s team to previous position", teamType));
        currentSet().getTeamComposition(teamType).rotateToPreviousPositions();
        notifyTeamRotated(teamType, false);
    }

    void notifyPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        Log.i(Tags.TEAM, String.format("Player #%d of %s team is on %s position", number, teamType, positionType));
        for (final TeamListener listener : mTeamListeners) {
            listener.onPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    void notifyStartingLineupSubmitted(TeamType teamType) {
        Log.i(Tags.TEAM, String.format("Submit the starting lineup for %s team", teamType));
        for (final TeamListener listener : mTeamListeners) {
            listener.onStartingLineupSubmitted(teamType);
        }
    }

    private void notifyTeamsSwapped(final TeamType leftTeamType, final TeamType rightTeamType, final ActionOriginType actionOriginType) {
        Log.i(Tags.TEAM, String.format("Changed sides: %s team is on left, %s team is on right", leftTeamType, rightTeamType));
        for (final TeamListener listener : mTeamListeners) {
            listener.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);
        }
    }

    void notifyTeamRotated(TeamType teamType, boolean clockwise) {
        Log.i(Tags.TEAM, String.format("%s team rotated", teamType));
        for (final TeamListener listener : mTeamListeners) {
            listener.onTeamRotated(teamType, clockwise);
        }
    }

    // Timeout

    @Override
    public int countRemainingTimeouts(TeamType teamType) {
        return currentSet().getRemainingTimeouts(teamType);
    }

    @Override
    public int countRemainingTimeouts(TeamType teamType, int setIndex) {
        int timeouts = 0;
        com.tonkar.volleyballreferee.engine.game.set.Set set = mSets.get(setIndex);

        if (set != null) {
            timeouts = set.getRemainingTimeouts(teamType);
        }

        return timeouts;
    }

    @Override
    public List<TimeoutDto> getCalledTimeouts(TeamType teamType) {
        return currentSet().getCalledTimeouts(teamType);
    }

    @Override
    public List<TimeoutDto> getCalledTimeouts(TeamType teamType, int setIndex) {
        List<TimeoutDto> timeouts = new ArrayList<>();
        com.tonkar.volleyballreferee.engine.game.set.Set set = mSets.get(setIndex);

        if (set != null) {
            timeouts = set.getCalledTimeouts(teamType);
        }

        return timeouts;
    }

    @Override
    public void callTimeout(final TeamType teamType) {
        final int oldCount = currentSet().getRemainingTimeouts(teamType);

        if (mRules.isTeamTimeouts() && oldCount > 0) {
            final int newCount = currentSet().removeTimeout(teamType);

            notifyTimeoutUpdated(teamType, mRules.getTeamTimeoutsPerSet(), newCount);
            notifyTimeoutCalled(teamType);
        }
    }

    private void undoTimeout(TeamType teamType) {
        final int newCount = currentSet().undoTimeout(teamType);
        notifyTimeoutUpdated(teamType, mRules.getTeamTimeoutsPerSet(), newCount);
    }

    private void notifyTimeoutCalled(TeamType teamType) {
        Log.i(Tags.TIMEOUT, "Team timeout is called");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTimeout(teamType, mRules.getTeamTimeoutDuration());
        }
    }

    private void notifyTimeoutUpdated(final TeamType teamType, final int maxCount, final int newCount) {
        Log.i(Tags.TIMEOUT, String.format("%s has %d timeouts left on %d", teamType, newCount, maxCount));
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTimeoutUpdated(teamType, maxCount, newCount);
        }
    }

    void notifyTechnicalTimeoutReached() {
        Log.i(Tags.TIMEOUT, "Technical timeout is reached");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onTechnicalTimeout(mRules.getTechnicalTimeoutDuration());
        }
    }

    private void notifyGameIntervalReached() {
        Log.i(Tags.TIMEOUT, "Game interval is reached");
        for (final TimeoutListener listener : mTimeoutListeners) {
            listener.onGameInterval(mRules.getGameIntervalDuration());
        }
    }

    @Override
    public void giveSanction(TeamType teamType, SanctionType sanctionType, int number) {
        SanctionDto sanction = new SanctionDto(sanctionType, number, currentSetIndex(), getPoints(TeamType.HOME),
                                               getPoints(TeamType.GUEST));
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

    private void undoSanction(TeamType teamType, SanctionDto sanction) {
        for (SanctionDto tmpSanction : getAllSanctions(teamType)) {
            if (tmpSanction.equals(sanction)) {
                if (TeamType.HOME.equals(teamType)) {
                    mHomeTeamSanctions.remove(tmpSanction);
                } else {
                    mGuestTeamSanctions.remove(tmpSanction);
                }
                notifySanctionUndone(teamType, sanction.getCard(), sanction.getNum());
            }
        }
    }

    private void notifySanctionGiven(TeamType teamType, SanctionType sanctionType, int number) {
        Log.i(Tags.SANCTION, String.format("Player %d of %s team was given a %s sanction", number, teamType, sanctionType));
        for (final SanctionListener listener : mSanctionListeners) {
            listener.onSanction(teamType, sanctionType, number);
        }
    }

    private void notifySanctionUndone(TeamType teamType, SanctionType sanctionType, int number) {
        Log.i(Tags.SANCTION, String.format("Player %d of %s team had a %s sanction undone", number, teamType, sanctionType));
        for (final SanctionListener listener : mSanctionListeners) {
            listener.onUndoSanction(teamType, sanctionType, number);
        }
    }

    @Override
    public List<SanctionDto> getAllSanctions(TeamType teamType) {
        return new ArrayList<>(TeamType.HOME.equals(teamType) ? mHomeTeamSanctions : mGuestTeamSanctions);
    }

    @Override
    public List<SanctionDto> getAllSanctions(TeamType teamType, int setIndex) {
        List<SanctionDto> sanctionsForSet = new ArrayList<>();

        for (SanctionDto sanction : getAllSanctions(teamType)) {
            if (sanction.getSet() == setIndex) {
                sanctionsForSet.add(sanction);
            }
        }

        return sanctionsForSet;
    }

    @Override
    public List<SanctionDto> getPlayerSanctions(TeamType teamType, int number) {
        List<SanctionDto> sanctionsForPlayer = new ArrayList<>();

        for (SanctionDto sanction : getAllSanctions(teamType)) {
            if (sanction.getNum() == number) {
                sanctionsForPlayer.add(sanction);
            }
        }

        return sanctionsForPlayer;
    }

    @Override
    public boolean hasSanctions(TeamType teamType, int number) {
        return getPlayerSanctions(teamType, number).size() > 0;
    }

    @Override
    public Set<Integer> getEvictedPlayersForCurrentSet(TeamType teamType, boolean withExpulsions, boolean withDisqualifications) {
        Set<Integer> players = new HashSet<>();

        List<SanctionDto> sanctionsForGame = getAllSanctions(teamType);
        int currentSetIndex = currentSetIndex();

        for (SanctionDto sanction : sanctionsForGame) {
            if (withDisqualifications && sanction.getCard().isMisconductDisqualificationCard()) {
                players.add(sanction.getNum());
            } else if (withExpulsions && sanction.getCard().isMisconductExpulsionCard() && sanction.getSet() == currentSetIndex) {
                players.add(sanction.getNum());
            }
        }

        return players;
    }

    @Override
    public SanctionType getMostSeriousSanction(TeamType teamType, int number) {
        SanctionType sanctionType = SanctionType.YELLOW;
        List<SanctionDto> playerSanctions = getPlayerSanctions(teamType, number);

        for (SanctionDto sanction : playerSanctions) {
            if (sanction.getCard().seriousness() > sanctionType.seriousness()) {
                sanctionType = sanction.getCard();
            }
        }

        return sanctionType;
    }

    @Override
    public SanctionType getPossibleDelaySanction(TeamType teamType) {
        boolean teamHasReachedPenalty = false;

        for (SanctionDto sanction : getAllSanctions(teamType)) {
            if (sanction.getCard().isDelaySanctionType()) {
                teamHasReachedPenalty = true;
                break;
            }
        }

        return teamHasReachedPenalty ? SanctionType.DELAY_PENALTY : SanctionType.DELAY_WARNING;
    }

    @Override
    public Set<SanctionType> getPossibleMisconductSanctions(TeamType teamType, int number) {
        boolean teamHasReachedPenalty = false;

        for (SanctionDto sanction : getAllSanctions(teamType)) {
            if (sanction.getCard().isMisconductSanctionType()) {
                teamHasReachedPenalty = true;
                break;
            }
        }

        Set<SanctionType> possibleMisconductSanctions = new HashSet<>();
        possibleMisconductSanctions.add(SanctionType.YELLOW);
        possibleMisconductSanctions.add(SanctionType.RED);
        possibleMisconductSanctions.add(SanctionType.RED_EXPULSION);
        possibleMisconductSanctions.add(SanctionType.RED_DISQUALIFICATION);

        if (teamHasReachedPenalty) {
            possibleMisconductSanctions.remove(SanctionType.YELLOW);
        }

        if (hasSanctions(teamType, number)) {
            SanctionType mostSeriousSanction = getMostSeriousSanction(teamType, number);
            possibleMisconductSanctions.removeIf(sanctionType -> sanctionType.seriousness() <= mostSeriousSanction.seriousness());
        }

        return possibleMisconductSanctions;
    }

    /* *******************************
     * Specific custom rules section *
     * *******************************/

    public boolean samePlayerServedNConsecutiveTimes(TeamType teamType, int teamPoints, List<TeamType> pointsLadder) {
        boolean result = false;

        int limit = getRules().getCustomConsecutiveServesPerPlayer();
        if (limit <= teamPoints) {
            int consecutiveServes = getConsecutiveServes(teamType, pointsLadder);

            if (consecutiveServes > 0 && consecutiveServes % limit == 0) {
                result = true;
            }
        }

        return result;
    }

    public boolean samePlayerHadServedNConsecutiveTimes(TeamType teamType, int teamPoints, List<TeamType> pointsLadder) {
        List<TeamType> tempPointsLadder = new ArrayList<>(pointsLadder);
        tempPointsLadder.add(teamType);
        int tempTeamPoints = teamPoints + 1;
        return samePlayerServedNConsecutiveTimes(teamType, tempTeamPoints, tempPointsLadder);
    }

    protected int getConsecutiveServes(TeamType teamType, List<TeamType> pointsLadder) {
        int consecutiveServes;
        int ladderIndex = pointsLadder.size() - 1;

        if (pointsLadder.isEmpty()) {
            consecutiveServes = 0;
        } else if (teamType.equals(pointsLadder.get(ladderIndex))) {
            List<TeamType> consecutivePoints = new ArrayList<>();
            while (ladderIndex >= 0 && teamType.equals(pointsLadder.get(ladderIndex))) {
                consecutivePoints.add(teamType);
                ladderIndex--;
            }
            consecutiveServes = consecutivePoints.size();

            // Side-out doesn't count as a serve
            if (ladderIndex >= 0 && !teamType.equals(pointsLadder.get(ladderIndex))) {
                consecutiveServes--;
            } else if (ladderIndex < 0 && !currentSet().getServingTeamAtStart().equals(teamType)) {
                consecutiveServes--;
            }
        } else {
            consecutiveServes = 0;
        }

        return consecutiveServes;
    }

    private void initTransientFields() {
        mEnableNotifications = true;
        mScoreListeners = new HashSet<>();
        mTimeoutListeners = new HashSet<>();
        mTeamListeners = new HashSet<>();
        mSanctionListeners = new HashSet<>();
    }

    @Override
    public boolean areNotificationsEnabled() {
        return mEnableNotifications;
    }

    @Override
    public List<GameEvent> getLatestGameEvents() {
        int currentSetIndex = currentSetIndex();
        int homePoints = getPoints(TeamType.HOME, currentSetIndex);
        int guestPoints = getPoints(TeamType.GUEST, currentSetIndex);
        List<GameEvent> gameEvents = new ArrayList<>();

        if (!getPointsLadder(currentSetIndex).isEmpty()) {
            gameEvents.add(GameEvent.newPointEvent(getServingTeam()));
        }

        for (TimeoutDto timeout : getCalledTimeouts(TeamType.HOME, currentSetIndex)) {
            if (timeout.getHomePoints() == homePoints && timeout.getGuestPoints() == guestPoints) {
                gameEvents.add(GameEvent.newTimeoutEvent(TeamType.HOME));
            }
        }

        for (TimeoutDto timeout : getCalledTimeouts(TeamType.GUEST, currentSetIndex)) {
            if (timeout.getHomePoints() == homePoints && timeout.getGuestPoints() == guestPoints) {
                gameEvents.add(GameEvent.newTimeoutEvent(TeamType.GUEST));
            }
        }

        Set<Integer> expulsedOrDisqualifiedPlayers = getEvictedPlayersForCurrentSet(TeamType.HOME, true, true);

        for (SubstitutionDto substitution : getSubstitutions(TeamType.HOME, currentSetIndex)) {
            if (substitution.getHomePoints() == homePoints && substitution.getGuestPoints() == guestPoints && !expulsedOrDisqualifiedPlayers.contains(
                    substitution.getPlayerIn()) && !expulsedOrDisqualifiedPlayers.contains(substitution.getPlayerOut())) {
                gameEvents.add(GameEvent.newSubstitutionEvent(TeamType.HOME, substitution));
            }
        }

        expulsedOrDisqualifiedPlayers = getEvictedPlayersForCurrentSet(TeamType.GUEST, true, true);

        for (SubstitutionDto substitution : getSubstitutions(TeamType.GUEST, currentSetIndex)) {
            if (substitution.getHomePoints() == homePoints && substitution.getGuestPoints() == guestPoints && !expulsedOrDisqualifiedPlayers.contains(
                    substitution.getPlayerIn()) && !expulsedOrDisqualifiedPlayers.contains(substitution.getPlayerOut())) {
                gameEvents.add(GameEvent.newSubstitutionEvent(TeamType.GUEST, substitution));
            }
        }

        for (SanctionDto sanction : getAllSanctions(TeamType.HOME, currentSetIndex)) {
            if (sanction.getHomePoints() == homePoints && sanction.getGuestPoints() == guestPoints) {
                gameEvents.add(GameEvent.newSanctionEvent(TeamType.HOME, sanction));
            }
        }

        for (SanctionDto sanction : getAllSanctions(TeamType.GUEST, currentSetIndex)) {
            if (sanction.getHomePoints() == homePoints && sanction.getGuestPoints() == guestPoints) {
                gameEvents.add(GameEvent.newSanctionEvent(TeamType.GUEST, sanction));
            }
        }

        return gameEvents;
    }

    @Override
    public void undoGameEvent(GameEvent gameEvent) {
        switch (gameEvent.getEventType()) {
            case POINT -> removeLastPoint();
            case TIMEOUT -> undoTimeout(gameEvent.getTeamType());
            case SUBSTITUTION -> undoSubstitution(gameEvent.getTeamType(), gameEvent.getSubstitution());
            case SANCTION -> undoSanction(gameEvent.getTeamType(), gameEvent.getSanction());
            default -> {
            }
        }
    }

    void forceFinishSet(TeamType teamType) {
        mEnableNotifications = false;

        // By using the variable currentSet, we add points until this set is finished
        com.tonkar.volleyballreferee.engine.game.set.Set currentSet = currentSet();

        while (!currentSet.isSetCompleted()) {
            addPoint(teamType);
        }

        mEnableNotifications = true;
    }

    void forceFinishMatch(TeamType teamType) {
        mEnableNotifications = false;

        // By using the function currentSet(), we add points until every set is finished (= match)
        while (!currentSet().isSetCompleted()) {
            addPoint(teamType);
        }

        mEnableNotifications = true;
    }

    @Override
    public void restoreTeams(IStoredGame storedGame) {
        restoreTeam(storedGame, TeamType.HOME);
        restoreTeam(storedGame, TeamType.GUEST);
    }

    void restoreTeam(IStoredGame storedGame, TeamType teamType) {
        setTeamId(teamType, storedGame.getTeamId(teamType));
        setCreatedBy(teamType, storedGame.getCreatedBy(teamType));
        setCreatedAt(teamType, storedGame.getCreatedAt(teamType));
        setUpdatedAt(teamType, storedGame.getUpdatedAt(teamType));
        setTeamName(teamType, storedGame.getTeamName(teamType));
        setTeamColor(teamType, storedGame.getTeamColor(teamType));
        setGender(teamType, storedGame.getGender(teamType));

        for (PlayerDto player : storedGame.getPlayers(teamType)) {
            addPlayer(teamType, player.getNum());
            if (!player.getName().trim().isEmpty()) {
                setPlayerName(teamType, player.getNum(), player.getName());
            }
        }
    }

    @Override
    public String getScore() {
        StringBuilder scoreBuilder = new StringBuilder();

        for (com.tonkar.volleyballreferee.engine.game.set.Set set : mSets) {
            scoreBuilder
                    .append(UiUtils.formatScoreFromLocale(set.getPoints(TeamType.HOME), set.getPoints(TeamType.GUEST), false))
                    .append("\t\t");
        }

        return scoreBuilder.toString().trim();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Game other) {
            result = super.equals(other) && (this.getUsage().equals(other.getUsage())) && (this.getKind().equals(other.getKind())) && (this
                    .getId()
                    .equals(other.getId())) && (this.getGender().equals(other.getGender())) && (this
                    .getRules()
                    .equals(other.getRules())) && (this.getLeague().equals(other.getLeague())) && (this
                    .getTeamDefinition(TeamType.HOME)
                    .equals(other.getTeamDefinition(TeamType.HOME))) && (this
                    .getTeamDefinition(TeamType.GUEST)
                    .equals(other.getTeamDefinition(TeamType.GUEST))) && (this
                    .getTeamOnLeftSide()
                    .equals(other.getTeamOnLeftSide())) && (this.getTeamOnRightSide().equals(other.getTeamOnRightSide()));

            if (result) {
                for (int setIndex = 0; setIndex < getNumberOfSets(); setIndex++) {
                    result = result && this.getSet(setIndex).equals(other.getSet(setIndex));
                }
            }
        }

        return result;
    }

}