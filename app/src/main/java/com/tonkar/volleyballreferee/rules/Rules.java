package com.tonkar.volleyballreferee.rules;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.data.UserId;

public class Rules {

    @SerializedName("userId")
    private final UserId  mUserId;
    @SerializedName("name")
    private final String  mName;
    @SerializedName("date")
    private final long    mDate;
    @SerializedName("setsPerGame")
    private final int     mSetsPerGame;
    @SerializedName("pointsPerSet")
    private final int     mPointsPerSet;
    @SerializedName("tieBreakInLastSet")
    private final boolean mTieBreakInLastSet;
    @SerializedName("pointsInTieBreak")
    private final int     mPointsInTieBreak;
    @SerializedName("twoPointsDifference")
    private final boolean m2PointsDifference;
    @SerializedName("sanctions")
    private final boolean mSanctionsEnabled;
    @SerializedName("teamTimeouts")
    private final boolean mTeamTimeoutsEnabled;
    @SerializedName("teamTimeoutsPerSet")
    private final int     mTeamTimeoutsPerSet;
    @SerializedName("teamTimeoutDuration")
    private final int     mTeamTimeoutDuration;
    @SerializedName("technicalTimeouts")
    private final boolean mTechnicalTimeoutsEnabled;
    @SerializedName("technicalTimeoutDuration")
    private final int     mTechnicalTimeoutDuration;
    @SerializedName("gameIntervals")
    private final boolean mGameIntervalsEnabled;
    @SerializedName("gameIntervalDuration")
    private final int     mGameIntervalDuration;
    @SerializedName("teamSubstitutionsPerSet")
    private final int     mTeamSubstitutionsPerSet;
    @SerializedName("changeSidesBeach")
    private final boolean mChangeSidesBeach;
    @SerializedName("changeSidesPeriod")
    private final int     mChangeSidesPeriod;
    @SerializedName("changeSidesPeriodTieBreak")
    private final int     mChangeSidesPeriodTieBreak;
    @SerializedName("customConsecutiveServesPerPlayer")
    private final int     mCustomConsecutiveServesPerPlayer;

    public Rules(final UserId userId, final String name, final long date,
                 final int setsPerGame, final int pointsPerSet, final boolean tieBreakInLastSet, final int pointsInTieBreak, final boolean twoPointsDifference, final boolean sanctionsEnabled,
                 final boolean teamTimeoutsEnabled, final int teamTimeoutsPerSet, final int teamTimeoutDuration,
                 final boolean technicalTimeoutsEnabled, final int technicalTimeoutDuration,
                 final boolean gameIntervalsEnabled, final int gameIntervalDuration,
                 final int teamSubstitutionsPerSet, final boolean changeSidesBeach, final int changeSidesPeriod, final int changeSidesPeriodTieBreak, final int customConsecutiveServesPerPlayer) {
        mUserId = userId;
        mName = name;
        mDate = date;

        mSetsPerGame = setsPerGame;
        mPointsPerSet = pointsPerSet;
        mTieBreakInLastSet = tieBreakInLastSet;
        mPointsInTieBreak = pointsInTieBreak;
        m2PointsDifference = twoPointsDifference;
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

    public boolean is2PointsDifference() {
        return m2PointsDifference;
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

    public void printRules() {
        Log.i("VBR-Rules", String.format("setsPerGame: %d", mSetsPerGame));
        Log.i("VBR-Rules", String.format("pointsPerSet: %d", mPointsPerSet));
        Log.i("VBR-Rules", String.format("tieBreakInLastSet: %b", mTieBreakInLastSet));
        Log.i("VBR-Rules", String.format("pointsInTieBreak: %d", mPointsInTieBreak));
        Log.i("VBR-Rules", String.format("twoPointsDifference: %b", m2PointsDifference));
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
                    && (this.is2PointsDifference() == other.is2PointsDifference())
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
