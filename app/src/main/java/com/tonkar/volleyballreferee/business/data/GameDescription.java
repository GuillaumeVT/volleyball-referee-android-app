package com.tonkar.volleyballreferee.business.data;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;

public class GameDescription {

    @SerializedName("userId")
    private String     mUserId;
    @SerializedName("kind")
    private GameType   mGameType;
    @SerializedName("date")
    private long       mGameDate;
    @SerializedName("schedule")
    private long       mGameSchedule;
    @SerializedName("gender")
    private GenderType mGenderType;
    @SerializedName("usage")
    private UsageType  mUsageType;
    @SerializedName("status")
    private GameStatus mGameStatus;
    @SerializedName("referee")
    private String     mRefereeName;
    @SerializedName("league")
    private String     mLeagueName;
    @SerializedName("hName")
    private String     mHomeTeamName;
    @SerializedName("gName")
    private String     mGuestTeamName;
    @SerializedName("hSets")
    private int        mHomeTeamSets;
    @SerializedName("gSets")
    private int        mGuestTeamSets;
    @SerializedName("rules")
    private String     mRulesName;

    public GameDescription() {
        mUserId = UserId.VBR_USER_ID;
        mGameType = GameType.INDOOR;
        mGameDate = 0L;
        mGameSchedule = 0L;
        mGenderType = GenderType.MIXED;
        mUsageType = UsageType.NORMAL;
        mGameStatus = GameStatus.COMPLETED;
        mRefereeName = "";
        mLeagueName = "";
        mHomeTeamName = "";
        mGuestTeamName = "";
        mHomeTeamSets = 0;
        mGuestTeamSets = 0;
        mRulesName = "";
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public GameType getGameType() {
        return mGameType;
    }

    public void setGameType(GameType gameType) {
        mGameType = gameType;
    }

    public long getGameDate() {
        return mGameDate;
    }

    public void setGameDate(long gameDate) {
        mGameDate = gameDate;
    }

    public long getGameSchedule() {
        return mGameSchedule;
    }

    public void setGameSchedule(long gameSchedule) {
        mGameSchedule = gameSchedule;
    }

    public GenderType getGenderType() {
        return mGenderType;
    }

    public void setGenderType(GenderType genderType) {
        mGenderType = genderType;
    }

    public UsageType getUsageType() {
        return mUsageType;
    }

    public void setUsageType(UsageType usageType) {
        mUsageType = usageType;
    }

    public GameStatus getMatchStatus() {
        return mGameStatus;
    }

    public void setMatchStatus(GameStatus gameStatus) {
        mGameStatus = gameStatus;
    }

    public String getRefereeName() {
        return mRefereeName;
    }

    public void setRefereeName(String name) {
        mRefereeName = name;
    }

    public String getLeagueName() {
        return mLeagueName;
    }

    public void setLeagueName(String name) {
        mLeagueName = name;
    }

    public String getHomeTeamName() {
        return mHomeTeamName;
    }

    public void setHomeTeamName(String homeTeamName) {
        mHomeTeamName = homeTeamName;
    }

    public String getGuestTeamName() {
        return mGuestTeamName;
    }

    public void setGuestTeamName(String guestTeamName) {
        mGuestTeamName = guestTeamName;
    }

    public int geHomeTeamSets() {
        return mHomeTeamSets;
    }

    public void setHomeTeamSets(int homeTeamSets) {
        mHomeTeamSets = homeTeamSets;
    }

    public int getGuestTeamSets() {
        return mGuestTeamSets;
    }

    public void setGuestTeamSets(int guestTeamSets) {
        mGuestTeamSets = guestTeamSets;
    }

    public String getRulesName() {
        return mRulesName;
    }

    public void setRulesName(String rulesName) {
        mRulesName = rulesName;
    }
    
}
