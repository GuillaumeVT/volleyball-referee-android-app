package com.tonkar.volleyballreferee.rules;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.data.UserId;

public class Rules {

    @SerializedName("userId")
    private UserId  mUserId;
    @SerializedName("name")
    private String  mName;
    @SerializedName("date")
    private long    mDate;
    @SerializedName("setsPerGame")
    private int     mSetsPerGame;
    @SerializedName("pointsPerSet")
    private int     mPointsPerSet;
    @SerializedName("tieBreakInLastSet")
    private boolean mTieBreakInLastSet;
    @SerializedName("pointsInTieBreak")
    private int     mPointsInTieBreak;
    @SerializedName("twoPointsDifference")
    private boolean mTwoPointsDifference;
    @SerializedName("sanctions")
    private boolean mSanctionsEnabled;
    @SerializedName("teamTimeouts")
    private boolean mTeamTimeoutsEnabled;
    @SerializedName("teamTimeoutsPerSet")
    private int     mTeamTimeoutsPerSet;
    @SerializedName("teamTimeoutDuration")
    private int     mTeamTimeoutDuration;
    @SerializedName("technicalTimeouts")
    private boolean mTechnicalTimeoutsEnabled;
    @SerializedName("technicalTimeoutDuration")
    private int     mTechnicalTimeoutDuration;
    @SerializedName("gameIntervals")
    private boolean mGameIntervalsEnabled;
    @SerializedName("gameIntervalDuration")
    private int     mGameIntervalDuration;
    @SerializedName("teamSubstitutionsPerSet")
    private int     mTeamSubstitutionsPerSet;
    @SerializedName("changeSidesBeach")
    private boolean mChangeSidesBeach;
    @SerializedName("changeSidesPeriod")
    private int     mChangeSidesPeriod;
    @SerializedName("changeSidesPeriodTieBreak")
    private int     mChangeSidesPeriodTieBreak;
    @SerializedName("customConsecutiveServesPerPlayer")
    private int     mCustomConsecutiveServesPerPlayer;

    public Rules(UserId userId, String name, long date,
                 int setsPerGame, int pointsPerSet, boolean tieBreakInLastSet, int pointsInTieBreak, boolean twoPointsDifference, boolean sanctionsEnabled,
                 boolean teamTimeoutsEnabled, int teamTimeoutsPerSet, int teamTimeoutDuration,
                 boolean technicalTimeoutsEnabled, int technicalTimeoutDuration,
                 boolean gameIntervalsEnabled, int gameIntervalDuration,
                 int teamSubstitutionsPerSet, boolean changeSidesBeach, int changeSidesPeriod, int changeSidesPeriodTieBreak, int customConsecutiveServesPerPlayer) {
        mUserId = userId;
        mName = name;
        mDate = date;

        mSetsPerGame = setsPerGame;
        mPointsPerSet = pointsPerSet;
        mTieBreakInLastSet = tieBreakInLastSet;
        mPointsInTieBreak = pointsInTieBreak;
        mTwoPointsDifference = twoPointsDifference;
        mSanctionsEnabled = sanctionsEnabled;

        mTeamTimeoutsEnabled = teamTimeoutsEnabled;
        mTeamTimeoutsPerSet = teamTimeoutsPerSet;
        mTeamTimeoutDuration = teamTimeoutDuration;

        mTechnicalTimeoutsEnabled = technicalTimeoutsEnabled;
        mTechnicalTimeoutDuration = technicalTimeoutDuration;

        mGameIntervalsEnabled = gameIntervalsEnabled;
        mGameIntervalDuration = gameIntervalDuration;

        mTeamSubstitutionsPerSet = teamSubstitutionsPerSet;
        mChangeSidesBeach = changeSidesBeach;
        mChangeSidesPeriod = changeSidesPeriod;
        mChangeSidesPeriodTieBreak = changeSidesPeriodTieBreak;

        mCustomConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;
    }

    public static final Rules DEFAULT_UNIVERSAL_RULES  = new Rules(UserId.VBR_USER_ID, "FIVB indoor 6x6 rules", 0L,
            5, 25, true, 15, true, true, true, 2, 30,
            true, 60, true, 180,
            6, true, 7, 5, 9999);
    public static final Rules OFFICIAL_INDOOR_RULES    = new Rules(UserId.VBR_USER_ID, "FIVB indoor 6x6 rules", 0L,
            5, 25, true, 15, true, true, true, 2, 30,
            true, 60, true, 180,
            6, false, 0, 0, 9999);
    public static final Rules OFFICIAL_BEACH_RULES     = new Rules(UserId.VBR_USER_ID, "FIVB beach rules", 0L,
            3, 21, true, 15, true, true, true, 1, 30,
            true, 30, true, 60,
            0, true, 7, 5, 9999);
    public static final Rules DEFAULT_INDOOR_4X4_RULES = new Rules(UserId.VBR_USER_ID, "Default 4x4 rules", 0L,
            5, 25, true, 15, true, true, true, 2, 30,
            true, 60, true, 180,
            4, false, 0, 0, 9999);

    public UserId getUserId() {
        return mUserId;
    }

    public String getName() {
        return mName;
    }

    public long getDate() {
        return mDate;
    }

    public int getSetsPerGame() {
        return mSetsPerGame;
    }

    public int getPointsPerSet() {
        return mPointsPerSet;
    }

    public boolean isTieBreakInLastSet() {
        return mTieBreakInLastSet;
    }

    public int getPointsInTieBreak() {
        return mPointsInTieBreak;
    }

    public boolean isTwoPointsDifference() {
        return mTwoPointsDifference;
    }

    public boolean areSanctionsEnabled() {
        return mSanctionsEnabled;
    }

    public boolean areTeamTimeoutsEnabled() {
        return mTeamTimeoutsEnabled;
    }

    public int getTeamTimeoutsPerSet() {
        return mTeamTimeoutsPerSet;
    }

    public int getTeamTimeoutDuration() {
        return mTeamTimeoutDuration;
    }

    public boolean areTechnicalTimeoutsEnabled() {
        return mTechnicalTimeoutsEnabled;
    }

    public int getTechnicalTimeoutDuration() {
        return mTechnicalTimeoutDuration;
    }

    public boolean areGameIntervalsEnabled() {
        return mGameIntervalsEnabled;
    }

    public int getGameIntervalDuration() {
        return mGameIntervalDuration;
    }

    public int getTeamSubstitutionsPerSet() {
        return mTeamSubstitutionsPerSet;
    }

    public boolean isChangeSidesBeach() {
        return mChangeSidesBeach;
    }

    public int getChangeSidesPeriod() {
        return mChangeSidesPeriod;
    }

    public int getChangeSidesPeriodTieBreak() {
        return mChangeSidesPeriodTieBreak;
    }

    public int getCustomConsecutiveServesPerPlayer() {
        return mCustomConsecutiveServesPerPlayer;
    }

    public void setUserId(UserId userId) {
        mUserId = userId;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDate(long date) {
        mDate = date;
    }

    public void setSetsPerGame(int setsPerGame) {
        mSetsPerGame = setsPerGame;
    }

    public void setPointsPerSet(int pointsPerSet) {
        mPointsPerSet = pointsPerSet;
    }

    public void setTieBreakInLastSet(boolean tieBreakInLastSet) {
        mTieBreakInLastSet = tieBreakInLastSet;
    }

    public void setPointsInTieBreak(int pointsInTieBreak) {
        mPointsInTieBreak = pointsInTieBreak;
    }

    public void setTwoPointsDifference(boolean twoPointsDifference) {
        mTwoPointsDifference = twoPointsDifference;
    }

    public void setSanctionsEnabled(boolean sanctionsEnabled) {
        mSanctionsEnabled = sanctionsEnabled;
    }

    public void setTeamTimeoutsEnabled(boolean teamTimeoutsEnabled) {
        mTeamTimeoutsEnabled = teamTimeoutsEnabled;
    }

    public void setTeamTimeoutsPerSet(int teamTimeoutsPerSet) {
        mTeamTimeoutsPerSet = teamTimeoutsPerSet;
    }

    public void setTeamTimeoutDuration(int teamTimeoutDuration) {
        mTeamTimeoutDuration = teamTimeoutDuration;
    }

    public void setTechnicalTimeoutsEnabled(boolean technicalTimeoutsEnabled) {
        mTechnicalTimeoutsEnabled = technicalTimeoutsEnabled;
    }

    public void setTechnicalTimeoutDuration(int technicalTimeoutDuration) {
        mTechnicalTimeoutDuration = technicalTimeoutDuration;
    }

    public void setGameIntervalsEnabled(boolean gameIntervalsEnabled) {
        mGameIntervalsEnabled = gameIntervalsEnabled;
    }

    public void setGameIntervalDuration(int gameIntervalDuration) {
        mGameIntervalDuration = gameIntervalDuration;
    }

    public void setTeamSubstitutionsPerSet(int teamSubstitutionsPerSet) {
        mTeamSubstitutionsPerSet = teamSubstitutionsPerSet;
    }

    public void setChangeSidesBeach(boolean changeSidesBeach) {
        mChangeSidesBeach = changeSidesBeach;
    }

    public void setChangeSidesPeriod(int changeSidesPeriod) {
        mChangeSidesPeriod = changeSidesPeriod;
    }

    public void setChangeSidesPeriodTieBreak(int changeSidesPeriodTieBreak) {
        mChangeSidesPeriodTieBreak = changeSidesPeriodTieBreak;
    }

    public void setCustomConsecutiveServesPerPlayer(int customConsecutiveServesPerPlayer) {
        mCustomConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;
    }

    public void printRules() {
        Log.i("VBR-Rules", String.format("setsPerGame: %d", mSetsPerGame));
        Log.i("VBR-Rules", String.format("pointsPerSet: %d", mPointsPerSet));
        Log.i("VBR-Rules", String.format("tieBreakInLastSet: %b", mTieBreakInLastSet));
        Log.i("VBR-Rules", String.format("pointsInTieBreak: %d", mPointsInTieBreak));
        Log.i("VBR-Rules", String.format("twoPointsDifference: %b", mTwoPointsDifference));
        Log.i("VBR-Rules", String.format("sanctions: %b", mSanctionsEnabled));
        Log.i("VBR-Rules", String.format("teamTimeouts: %b", mTeamTimeoutsEnabled));
        Log.i("VBR-Rules", String.format("teamTimeoutsPerSet: %d", mTeamTimeoutsPerSet));
        Log.i("VBR-Rules", String.format("teamTimeoutDuration: %d", mTeamTimeoutDuration));
        Log.i("VBR-Rules", String.format("technicalTimeouts: %b", mTechnicalTimeoutsEnabled));
        Log.i("VBR-Rules", String.format("technicalTimeoutDuration: %d", mTechnicalTimeoutDuration));
        Log.i("VBR-Rules", String.format("gameIntervals: %b", mGameIntervalsEnabled));
        Log.i("VBR-Rules", String.format("gameIntervalDuration: %d", mGameIntervalDuration));
        Log.i("VBR-Rules", String.format("teamSubstitutionsPerSet: %d", mTeamSubstitutionsPerSet));
        Log.i("VBR-Rules", String.format("changeSidesBeach: %b", mChangeSidesBeach));
        Log.i("VBR-Rules", String.format("changeSidesPeriod: %d", mChangeSidesPeriod));
        Log.i("VBR-Rules", String.format("changeSidesPeriodTieBreak: %d", mChangeSidesPeriodTieBreak));
        Log.i("VBR-Rules", String.format("customConsecutiveServesPerPlayer: %d", mCustomConsecutiveServesPerPlayer));
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Rules) {
            Rules other = (Rules) obj;
            result = this.getUserId().equals(other.getUserId())
                    && this.getName().equals(other.getName())
                    && (this.getDate() == other.getDate())
                    && (this.getSetsPerGame() == other.getSetsPerGame())
                    && (this.getPointsPerSet() == other.getPointsPerSet())
                    && (this.isTieBreakInLastSet() == other.isTieBreakInLastSet())
                    && (this.getPointsInTieBreak() == other.getPointsInTieBreak())
                    && (this.isTwoPointsDifference() == other.isTwoPointsDifference())
                    && (this.areSanctionsEnabled() == other.areSanctionsEnabled())
                    && (this.areTeamTimeoutsEnabled() == other.areTeamTimeoutsEnabled())
                    && (this.getTeamTimeoutsPerSet() == other.getTeamTimeoutsPerSet())
                    && (this.getTeamTimeoutDuration() == other.getTeamTimeoutDuration())
                    && (this.areTechnicalTimeoutsEnabled() == other.areTechnicalTimeoutsEnabled())
                    && (this.getTechnicalTimeoutDuration() == other.getTechnicalTimeoutDuration())
                    && (this.areGameIntervalsEnabled() == other.areGameIntervalsEnabled())
                    && (this.getGameIntervalDuration() == other.getGameIntervalDuration())
                    && (this.getTeamSubstitutionsPerSet() == other.getTeamSubstitutionsPerSet())
                    && (this.isChangeSidesBeach() == other.isChangeSidesBeach())
                    && (this.getChangeSidesPeriod() == other.getChangeSidesPeriod())
                    && (this.getChangeSidesPeriodTieBreak() == other.getChangeSidesPeriodTieBreak())
                    && (this.getCustomConsecutiveServesPerPlayer() == other.getCustomConsecutiveServesPerPlayer());
        }

        return result;
    }
}
