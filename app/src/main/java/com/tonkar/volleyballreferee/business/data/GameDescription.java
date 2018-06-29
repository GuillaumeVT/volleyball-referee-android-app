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
    @SerializedName("indexed")
    private boolean    mIndexed;
    @SerializedName("referee")
    private String     mRefereeName;
    @SerializedName("league")
    private String     mLeagueName;
    @SerializedName("division")
    private String     mDivisionName;
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
        mIndexed = true;
        mRefereeName = "";
        mLeagueName = "";
        mDivisionName = "";
        mHomeTeamName = "";
        mGuestTeamName = "";
        mHomeTeamSets = 0;
        mGuestTeamSets = 0;
        mRulesName = "";
    }

    public String getUserId() {
        return mUserId;
    }

    public GameType getGameType() {
        return mGameType;
    }

    public long getGameDate() {
        return mGameDate;
    }

    public long getGameSchedule() {
        return mGameSchedule;
    }

    public GenderType getGenderType() {
        return mGenderType;
    }

    public UsageType getUsageType() {
        return mUsageType;
    }

    public GameStatus getMatchStatus() {
        return mGameStatus;
    }

    public boolean isIndexed() {
        return mIndexed;
    }

    public String getRefereeName() {
        return mRefereeName;
    }

    public String getLeagueName() {
        return mLeagueName;
    }

    public String getDivisionName() {
        return mDivisionName;
    }

    public String getHomeTeamName() {
        return mHomeTeamName;
    }

    public String getGuestTeamName() {
        return mGuestTeamName;
    }

    public int geHomeTeamSets() {
        return mHomeTeamSets;
    }

    public int getGuestTeamSets() {
        return mGuestTeamSets;
    }

    public String getRulesName() {
        return mRulesName;
    }

}
