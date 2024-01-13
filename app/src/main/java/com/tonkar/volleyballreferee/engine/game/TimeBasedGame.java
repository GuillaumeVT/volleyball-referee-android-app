package com.tonkar.volleyballreferee.engine.game;

import android.graphics.Color;
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
import com.tonkar.volleyballreferee.engine.team.definition.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.*;

public class TimeBasedGame extends BaseGame implements ITimeBasedGame {

    @SerializedName("id")
    private       String            mId;
    @SerializedName("createdBy")
    private       String            mCreatedBy;
    @SerializedName("createdAt")
    private       long              mCreatedAt;
    @SerializedName("updatedAt")
    private       long              mUpdatedAt;
    @SerializedName("scheduledAt")
    private       long              mScheduledAt;
    @SerializedName("refereedBy")
    private       String            mRefereedBy;
    @SerializedName("refereeName")
    private       String            mRefereeName;
    @SerializedName("gender")
    private       GenderType        mGender;
    @SerializedName("gameStatus")
    private       GameStatus        mGameStatus;
    @SerializedName("indexed")
    private       boolean           mIndexed;
    @SerializedName("league")
    private final ApiSelectedLeague mLeague;
    @SerializedName("homeTeam")
    private final TeamDefinition    mHomeTeam;
    @SerializedName("guestTeam")
    private final TeamDefinition    mGuestTeam;
    @SerializedName("teamOnLeftSide")
    private       TeamType          mTeamOnLeftSide;
    @SerializedName("teamOnRightSide")
    private       TeamType          mTeamOnRightSide;
    @SerializedName("homeTeamPoints")
    private       int               mHomeTeamPoints;
    @SerializedName("guestTeamPoints")
    private       int               mGuestTeamPoints;
    @SerializedName("pointsLadder")
    private final List<TeamType>    mPointsLadder;
    @SerializedName("duration")
    private       long              mDuration;
    @SerializedName("startTime")
    private       long              mStartTime;
    @SerializedName("endTime")
    private       long              mEndTime;
    @SerializedName("isStopped")
    private       boolean           mIsStopped;
    @SerializedName("servingTeamAtStart")
    private       TeamType          mServingTeamAtStart;

    private transient java.util.Set<GeneralListener> mGeneralListeners;
    private transient java.util.Set<ScoreListener>   mScoreListeners;
    private transient java.util.Set<TeamListener>    mTeamListeners;

    TimeBasedGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt) {
        super();
        mId = id;
        mCreatedBy = createdBy;
        mCreatedAt = createdAt;
        mUpdatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        mScheduledAt = scheduledAt;
        mRefereedBy = createdBy;
        mRefereeName = refereeName;
        mGender = GenderType.MIXED;
        mGameStatus = GameStatus.SCHEDULED;
        mIndexed = true;
        mLeague = new ApiSelectedLeague();
        mLeague.setId(UUID.randomUUID().toString());
        mLeague.setCreatedBy(createdBy);
        mLeague.setCreatedAt(createdAt);
        mLeague.setUpdatedAt(createdAt);
        mLeague.setKind(getKind());
        mLeague.setName("");
        mLeague.setDivision("");
        mHomeTeam = new EmptyTeamDefinition(UUID.randomUUID().toString(), createdBy, TeamType.HOME);
        mGuestTeam = new EmptyTeamDefinition(UUID.randomUUID().toString(), createdBy, TeamType.GUEST);
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

    // For GSON Deserialization
    public TimeBasedGame() {
        this("", "", "", 0L, 0L);
    }

    @Override
    public void addGeneralListener(GeneralListener listener) {
        mGeneralListeners.add(listener);
    }

    @Override
    public void removeGeneralListener(GeneralListener listener) {
        mGeneralListeners.remove(listener);
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
        return TeamType.HOME.equals(teamType) ? mHomeTeam : mGuestTeam;
    }

    @Override
    public void addSanctionListener(SanctionListener listener) {}

    @Override
    public void removeSanctionListener(SanctionListener listener) {}

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
        return GameType.TIME;
    }

    @Override
    public boolean isIndexed() {
        return mIndexed;
    }

    @Override
    public void setIndexed(boolean indexed) {
        mIndexed = indexed;

        for (GeneralListener listener : mGeneralListeners) {
            listener.onMatchIndexed(indexed);
        }
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
        return "";
    }

    @Override
    public void setReferee1Name(String referee1Name) {}

    @Override
    public String getReferee2Name() {
        return "";
    }

    @Override
    public void setReferee2Name(String referee2Name) {}

    @Override
    public String getScorerName() {
        return "";
    }

    @Override
    public void setScorerName(String scorerName) {}

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
        return GameType.TIME;
    }

    @Override
    public String getTeamName(TeamType teamType) {
        return getTeamDefinition(teamType).getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return getTeamDefinition(teamType).getColorInt();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {
        getTeamDefinition(teamType).setName(name);
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        getTeamDefinition(teamType).setColorInt(color);
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
    public java.util.Set<ApiPlayer> getPlayers(TeamType teamType) {
        return new TreeSet<>();
    }

    @Override
    public void setPlayerName(TeamType teamType, int number, String name) {}

    @Override
    public String getPlayerName(TeamType teamType, int number) {
        return "";
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
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return getTeamDefinition(TeamType.HOME).getExpectedNumberOfPlayersOnCourt();
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return Color.parseColor(TeamDefinition.DEFAULT_COLOR);
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {}

    @Override
    public void addLibero(TeamType teamType, int number) {}

    @Override
    public void removeLibero(TeamType teamType, int number) {}

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return false;
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return false;
    }

    @Override
    public java.util.Set<ApiPlayer> getLiberos(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    public List<ApiSubstitution> getSubstitutions(TeamType teamType) {
        return new ArrayList<>();
    }

    @Override
    public List<ApiSubstitution> getSubstitutions(TeamType teamType, int setIndex) {
        return new ArrayList<>();
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType) {
        return true;
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType, int setIndex) {
        return true;
    }

    @Override
    public ApiCourt getStartingLineup(TeamType teamType, int setIndex) {
        return new ApiCourt();
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        return PositionType.BENCH;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        return -1;
    }

    @Override
    public void setCaptain(TeamType teamType, int number) {}

    @Override
    public int getCaptain(TeamType teamType) {
        return -1;
    }

    @Override
    public java.util.Set<Integer> getPossibleCaptains(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return false;
    }

    @Override
    public String getCoachName(TeamType teamType) {
        return "";
    }

    @Override
    public void setCoachName(TeamType teamType, String name) {}

    @Override
    public void restoreTeams(IStoredGame storedGame) {}

    @Override
    public void callTimeout(TeamType teamType) {}

    @Override
    public int countRemainingTimeouts(TeamType teamType) {
        return 0;
    }

    @Override
    public int countRemainingTimeouts(TeamType teamType, int setIndex) {
        return 0;
    }

    @Override
    public List<ApiTimeout> getCalledTimeouts(TeamType teamType) {
        return new ArrayList<>();
    }

    @Override
    public List<ApiTimeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        return new ArrayList<>();
    }

    @Override
    public GameStatus getMatchStatus() {
        return mGameStatus;
    }

    @Override
    public String getGameSummary() {
        return String.format(Locale.getDefault(), "%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), mHomeTeamPoints, mGuestTeamPoints,
                             mGuestTeam.getName());
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
    public int getPoints(TeamType teamType) {
        return TeamType.HOME.equals(teamType) ? mHomeTeamPoints : mGuestTeamPoints;
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
    public TeamType getFirstServingTeam() {
        return mServingTeamAtStart;
    }

    @Override
    public TeamType getFirstServingTeam(int setIndex) {
        return mServingTeamAtStart;
    }

    @Override
    public String getScore() {
        return String.format(Locale.getDefault(), "%d-%d", mHomeTeamPoints, mGuestTeamPoints);
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
    public long getSetStartTime(int setIndex) {
        return mStartTime;
    }

    @Override
    public long getSetEndTime(int setIndex) {
        return mEndTime;
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
        Log.i(Tags.TEAM, String.format("Changed sides: %s team is on left, %s team is on right", leftTeamType, rightTeamType));
        for (final TeamListener listener : mTeamListeners) {
            listener.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);
        }
    }

    @Override
    public UsageType getUsage() {
        return UsageType.POINTS_SCOREBOARD;
    }

    @Override
    public void setUsage(UsageType usageType) {}

    @Override
    public Rules getRules() {
        return Rules.officialIndoorRules();
    }

    @Override
    public ApiSelectedLeague getLeague() {
        return mLeague;
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
        int newCount;

        if (TeamType.HOME.equals(teamType)) {
            mHomeTeamPoints++;
            newCount = mHomeTeamPoints;
        } else {
            mGuestTeamPoints++;
            newCount = mGuestTeamPoints;
        }

        mPointsLadder.add(teamType);

        notifyPointsUpdated(teamType, newCount);

        final TeamType newServingTeam = getServingTeam();

        if (!oldServingTeam.equals(newServingTeam)) {
            notifyServiceSwapped(newServingTeam, false);
        }
    }

    private void removeLastPoint() {
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
                notifyServiceSwapped(newServingTeam, false);
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

            notifyServiceSwapped(mServingTeamAtStart, true);
        }
    }

    private void notifyPointsUpdated(final TeamType teamType, int newCount) {
        Log.i(Tags.SCORE, String.format("Points are updated for %s team: %d", teamType, newCount));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onPointsUpdated(teamType, newCount);
        }
    }

    private void notifyServiceSwapped(final TeamType servingTeam, boolean isStart) {
        Log.i(Tags.SCORE, String.format("%s team is now serving", servingTeam));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onServiceSwapped(servingTeam, isStart);
        }
    }

    private void notifyMatchCompleted(TeamType winner) {
        Log.i(Tags.SCORE, String.format("Match is completed and %s team won", winner));

        for (final ScoreListener listener : mScoreListeners) {
            listener.onSetCompleted();
        }

        for (final ScoreListener listener : mScoreListeners) {
            listener.onMatchCompleted(winner);
        }
    }

    private void initTransientFields() {
        mGeneralListeners = new HashSet<>();
        mScoreListeners = new HashSet<>();
        mTeamListeners = new HashSet<>();
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
        mGameStatus = GameStatus.LIVE;
    }

    @Override
    public void stop() {
        if (isMatchRunning()) {
            mIsStopped = true;
            final TeamType winner = getPoints(TeamType.HOME) > getPoints(TeamType.GUEST) ? TeamType.HOME : TeamType.GUEST;
            mGameStatus = GameStatus.COMPLETED;
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

    @Override
    public void giveSanction(TeamType teamType, SanctionType sanctionType, int number) {}

    @Override
    public java.util.Set<Integer> getEvictedPlayersForCurrentSet(TeamType teamType, boolean withExpulsions, boolean withDisqualifications) {
        return new HashSet<>();
    }

    @Override
    public SanctionType getMostSeriousSanction(TeamType teamType, int number) {
        return SanctionType.YELLOW;
    }

    @Override
    public SanctionType getPossibleDelaySanction(TeamType teamType) {
        return null;
    }

    @Override
    public Set<SanctionType> getPossibleMisconductSanctions(TeamType teamType, int number) {
        return new HashSet<>();
    }

    @Override
    public List<ApiSanction> getAllSanctions(TeamType teamType) {
        return new ArrayList<>();
    }

    @Override
    public List<ApiSanction> getAllSanctions(TeamType teamType, int setIndex) {
        return new ArrayList<>();
    }

    @Override
    public List<ApiSanction> getPlayerSanctions(TeamType teamType, int number) {
        return new ArrayList<>();
    }

    @Override
    public boolean hasSanctions(TeamType teamType, int number) {
        return false;
    }

    @Override
    public boolean areNotificationsEnabled() {
        return true;
    }

    @Override
    public void restoreGame(IStoredGame storedGame) {}

    @Override
    public List<GameEvent> getLatestGameEvents() {
        return new ArrayList<>();
    }

    @Override
    public void undoGameEvent(GameEvent gameEvent) {
        removeLastPoint();
    }

    @Override
    public void startMatch() {
        GenderType homeGender = getGender(TeamType.HOME);
        GenderType guestGender = getGender(TeamType.GUEST);

        if (homeGender.equals(guestGender)) {
            mGender = homeGender;
        } else {
            mGender = GenderType.MIXED;
        }

        if (mScheduledAt == 0L) {
            mScheduledAt = System.currentTimeMillis();
        }
    }

    @Override
    public void resetCurrentSet() {}
}
