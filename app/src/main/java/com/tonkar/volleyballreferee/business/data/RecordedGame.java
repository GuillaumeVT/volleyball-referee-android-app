package com.tonkar.volleyballreferee.business.data;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.rules.Rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class RecordedGame implements RecordedGameService {

    @SerializedName("userId")
    private UserId            mUserId;
    @SerializedName("kind")
    private GameType          mGameType;
    @SerializedName("date")
    private long              mGameDate;
    @SerializedName("schedule")
    private long              mGameSchedule;
    @SerializedName("gender")
    private GenderType        mGenderType;
    @SerializedName("usage")
    private UsageType         mUsageType;
    @SerializedName("status")
    private GameStatus        mGameStatus;
    @SerializedName("referee")
    private String            mRefereeName;
    @SerializedName("league")
    private String            mLeagueName;
    @SerializedName("hTeam")
    private RecordedTeam      mHomeTeam;
    @SerializedName("gTeam")
    private RecordedTeam      mGuestTeam;
    @SerializedName("hSets")
    private int               mHomeSets;
    @SerializedName("gSets")
    private int               mGuestSets;
    @SerializedName("sets")
    private List<RecordedSet> mSets;
    @SerializedName("hCards")
    private List<Sanction>    mHomeSanctions;
    @SerializedName("gCards")
    private List<Sanction>    mGuestSanctions;
    @SerializedName("rules")
    private Rules             mRules;
    private transient boolean mIsRecordedOnline;

    public RecordedGame() {
        mUserId = UserId.VBR_USER_ID;
        mGameType = GameType.INDOOR;
        mGameDate = 0L;
        mGameSchedule = 0L;
        mGenderType = GenderType.MIXED;
        mUsageType = UsageType.NORMAL;
        mGameStatus = GameStatus.COMPLETED;
        mRefereeName = "";
        mLeagueName = "";
        mHomeTeam = new RecordedTeam();
        mGuestTeam = new RecordedTeam();
        mHomeSets = 0;
        mGuestSets = 0;
        mSets = new ArrayList<>();
        mHomeSanctions = new ArrayList<>();
        mGuestSanctions = new ArrayList<>();
        mRules = Rules.OFFICIAL_INDOOR_RULES;
        mIsRecordedOnline = false;
    }

    @Override
    public String getGameSummary() {
        String summary = String.format(Locale.getDefault(),"%s\t\t%d\t-\t%d\t\t%s\n", mHomeTeam.getName(), getSets(TeamType.HOME), getSets(TeamType.GUEST), mGuestTeam.getName());

        if (mIsRecordedOnline) {
            final String url;

            switch(mGameType) {
                case INDOOR_4X4:
                    url = WebUtils.VIEW_INDOOR_4X4_URL;
                    break;
                case BEACH:
                    url = WebUtils.VIEW_BEACH_URL;
                    break;
                case TIME:
                    url = WebUtils.VIEW_TIME_BASED_URL;
                    break;
                case INDOOR:
                default:
                    url = WebUtils.VIEW_INDOOR_URL;
                    break;
            }

            summary = summary + "\n" + String.format(Locale.getDefault(), url, mGameDate);
        }

        return summary;
    }

    int currentSetIndex() {
        return mSets.size() -1;
    }

    public RecordedTeam getTeam(TeamType teamType) {
        RecordedTeam team;

        if (TeamType.HOME.equals(teamType)) {
            team = mHomeTeam;
        } else {
            team = mGuestTeam;
        }

        return team;
    }

    public List<RecordedSet> getSets() {
        return mSets;
    }

    @Override
    public UserId getUserId() {
        return mUserId;
    }

    public void setUserId(UserId userId) {
        mUserId = userId;
    }

    @Override
    public GameType getGameType() {
        return mGameType;
    }

    public void setGameType(GameType gameType) {
        mGameType = gameType;
    }

    @Override
    public long getGameDate() {
        return mGameDate;
    }

    public void setGameDate(long gameDate) {
        mGameDate = gameDate;
    }

    @Override
    public long getGameSchedule() {
        return mGameSchedule;
    }

    public void setGameSchedule(long gameSchedule) {
        mGameSchedule = gameSchedule;
    }

    @Override
    public int getNumberOfSets() {
        return mSets.size();
    }

    @Override
    public int getSets(TeamType teamType) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = mHomeSets;
        } else {
            count = mGuestSets;
        }

        return count;
    }

    public void setSets(TeamType teamType, int count) {
        if (TeamType.HOME.equals(teamType)) {
            mHomeSets = count;
        } else {
            mGuestSets = count;
        }
    }

    @Override
    public long getSetDuration(int setIndex) {
        return mSets.get(setIndex).getDuration();
    }

    @Override
    public int getPoints(TeamType teamType) {
        return getPoints(teamType, currentSetIndex());
    }

    @Override
    public int getPoints(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getPoints(teamType);
    }

    @Override
    public List<TeamType> getPointsLadder() {
        return getPointsLadder(currentSetIndex());
    }

    @Override
    public List<TeamType> getPointsLadder(int setIndex) {
        return mSets.get(setIndex).getPointsLadder();
    }

    @Override
    public TeamType getServingTeam() {
        return getServingTeam(currentSetIndex());
    }

    @Override
    public TeamType getServingTeam(int setIndex) {
        return mSets.get(setIndex).getServingTeam();
    }

    @Override
    public GameStatus getMatchStatus() {
        return mGameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        mGameStatus = gameStatus;
    }

    @Override
    public boolean isMatchCompleted() {
        return GameStatus.COMPLETED.equals(mGameStatus);
    }

    @Override
    public UsageType getUsageType() {
        return mUsageType;
    }

    @Override
    public void setUsageType(UsageType usageType) {
        mUsageType = usageType;
    }

    @Override
    public Rules getRules() {
        return mRules;
    }

    public void setRules(Rules rules) {
        mRules = rules;
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
        return getTeam(teamType).getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return getTeam(teamType).getColor();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {}

    @Override
    public void setTeamColor(TeamType teamType, int color) {}

    @Override
    public void addPlayer(TeamType teamType, int number) {}

    @Override
    public void removePlayer(TeamType teamType, int number) {}

    @Override
    public boolean hasPlayer(TeamType teamType, int number) {
        boolean result;

        if (TeamType.HOME.equals(teamType)) {
            result = mHomeTeam.getPlayers().contains(number) || mHomeTeam.getLiberos().contains(number);
        } else {
            result = mGuestTeam.getPlayers().contains(number) || mGuestTeam.getLiberos().contains(number);
        }

        return result;
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        int count;

        if (TeamType.HOME.equals(teamType)) {
            count = mHomeTeam.getPlayers().size() + mHomeTeam.getLiberos().size();
        } else {
            count = mGuestTeam.getPlayers().size() + mGuestTeam.getLiberos().size();
        }

        return count;
    }

    @Override
    public Set<Integer> getPlayers(TeamType teamType) {
        Set<Integer> players = new TreeSet<>();
        players.addAll(getTeam(teamType).getPlayers());
        players.addAll(getTeam(teamType).getLiberos());
        return players;
    }

    @Override
    public GenderType getGenderType() {
        return mGenderType;
    }

    @Override
    public GenderType getGenderType(TeamType teamType) {
        return getTeam(teamType).getGenderType();
    }

    @Override
    public void setGenderType(GenderType genderType) {
        mGenderType = genderType;
    }

    @Override
    public void setGenderType(TeamType teamType, GenderType genderType) {
        getTeam(teamType).setGenderType(genderType);
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return 0;
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return getTeam(teamType).getLiberoColor();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {}

    @Override
    public void addLibero(TeamType teamType, int number) {}

    @Override
    public void removeLibero(TeamType teamType, int number) {}

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return getTeam(teamType).getLiberos().contains(number);
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return false;
    }

    @Override
    public Set<Integer> getLiberos(TeamType teamType) {
        return getTeam(teamType).getLiberos();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType) {
        return getSubstitutions(teamType, currentSetIndex());
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getSubstitutions(teamType);
    }

    @Override
    public boolean isStartingLineupConfirmed() {
        RecordedSet set = mSets.get(currentSetIndex());
        return !set.getStartingPlayers(TeamType.HOME).isEmpty() && !set.getStartingPlayers(TeamType.GUEST).isEmpty();
    }

    @Override
    public Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex) {
        Set<Integer> players = new TreeSet<>();
        List<RecordedPlayer> startingLineup = mSets.get(setIndex).getStartingPlayers(teamType);

        for (RecordedPlayer player : startingLineup) {
            players.add(player.getNumber());
        }

        return players;
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        PositionType positionType = null;
        List<RecordedPlayer> startingLineup = mSets.get(setIndex).getStartingPlayers(teamType);

        for (RecordedPlayer player : startingLineup) {
            if (player.getNumber() == number) {
                positionType = player.getPositionType();
            }
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        int number = -1;
        List<RecordedPlayer> startingLineup = mSets.get(setIndex).getStartingPlayers(teamType);

        for (RecordedPlayer player : startingLineup) {
            if (player.getPositionType().equals(positionType)) {
                number = player.getNumber();
            }
        }

        return number;
    }

    @Override
    public void setCaptain(TeamType teamType, int number) {
        getTeam(teamType).setCaptain(number);
    }

    @Override
    public int getCaptain(TeamType teamType) {
        return getTeam(teamType).getCaptain();
    }

    @Override
    public Set<Integer> getPossibleCaptains(TeamType teamType) {
        Set<Integer> possibleCaptains = new TreeSet<>();

        for (int number : getTeam(teamType).getPlayers()) {
            if (!isLibero(teamType, number)) {
                possibleCaptains.add(number);
            }
        }

        return possibleCaptains;
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return number == getTeam(teamType).getCaptain();
    }

    @Override
    public int getRemainingTimeouts(TeamType teamType) {
        return mSets.get(currentSetIndex()).getTimeouts(teamType);
    }

    @Override
    public int getRemainingTimeouts(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getTimeouts(teamType);
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType) {
        return mSets.get(currentSetIndex()).getCalledTimeouts(teamType);
    }

    @Override
    public List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex) {
        return mSets.get(setIndex).getCalledTimeouts(teamType);
    }

    @Override
    public long getRemainingTime() {
        return mSets.get(currentSetIndex()).getRemainingTime();
    }

    @Override
    public long getRemainingTime(int setIndex) {
        return mSets.get(setIndex).getRemainingTime();
    }

    @Override
    public boolean matchesFilter(String text) {
        return  text.isEmpty()
                || mHomeTeam.getName().toLowerCase(Locale.getDefault()).contains(text)
                || mGuestTeam.getName().toLowerCase(Locale.getDefault()).contains(text)
                || mLeagueName.toLowerCase(Locale.getDefault()).contains(text);
    }

    @Override
    public boolean isRecordedOnline() {
        return mIsRecordedOnline;
    }

    @Override
    public void setRecordedOnline(boolean recordedOnline) {
        mIsRecordedOnline = recordedOnline;
    }

    @Override
    public List<Sanction> getGivenSanctions(TeamType teamType) {
        List<Sanction> sanctions;

        if (TeamType.HOME.equals(teamType)) {
            sanctions = mHomeSanctions;
        } else {
            sanctions = mGuestSanctions;
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
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedGame) {
            RecordedGame other = (RecordedGame) obj;
            result = this.getUserId().equals(other.getUserId())
                    && (this.getGameDate() == other.getGameDate())
                    && (this.getGameSchedule() == other.getGameSchedule())
                    && this.getGameType().equals(other.getGameType())
                    && (this.getGenderType().equals(other.getGenderType()))
                    && (this.getUsageType().equals(other.getUsageType()))
                    && (this.isMatchCompleted() == other.isMatchCompleted())
                    && (this.getRefereeName().equals(other.getRefereeName()))
                    && (this.getLeagueName().equals(other.getLeagueName()))
                    && this.getTeam(TeamType.HOME).equals(other.getTeam(TeamType.HOME))
                    && this.getTeam(TeamType.GUEST).equals(other.getTeam(TeamType.GUEST))
                    && (this.getSets(TeamType.HOME) == other.getSets(TeamType.HOME))
                    && (this.getSets(TeamType.GUEST) == other.getSets(TeamType.GUEST))
                    && this.getSets().equals(other.getSets())
                    && this.getGivenSanctions(TeamType.HOME).equals(other.getGivenSanctions(TeamType.HOME))
                    && this.getGivenSanctions(TeamType.GUEST).equals(other.getGivenSanctions(TeamType.GUEST))
                    && this.getRules().equals(other.getRules());
        }

        return result;
    }
}
