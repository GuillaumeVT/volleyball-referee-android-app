package com.tonkar.volleyballreferee.rules;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.business.web.Authentication;
import com.tonkar.volleyballreferee.interfaces.Tags;

public class Rules {

    public static final transient int SINGLE_SUBSTITUTE_TYPE  = 1;
    public static final transient int PLURAL_SUBSTITUTES_TYPE = 2;
    public static final transient int FREE_SUBSTITUTIONS_TYPE = 3;

    @SerializedName("userId")
    private String  mUserId;
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
    @SerializedName("substitutionType")
    private int     mSubstitutionType;
    @SerializedName("teamSubstitutionsPerSet")
    private int     mTeamSubstitutionsPerSet;
    @SerializedName("beachCourtSwitches")
    private boolean mBeachCourtSwitchesEnabled;
    @SerializedName("beachCourtSwitchFreq")
    private int     mBeachCourtSwitchFrequency;
    @SerializedName("beachCourtSwitchFreqTieBreak")
    private int     mBeachCourtSwitchFrequencyTieBreak;
    @SerializedName("customConsecutiveServesPerPlayer")
    private int     mCustomConsecutiveServesPerPlayer;

    // For GSON Deserialization
    public Rules() {
        setAll(defaultUniversalRules());
    }

    public Rules(String userId, String name, long date,
                 int setsPerGame, int pointsPerSet, boolean tieBreakInLastSet, int pointsInTieBreak, boolean twoPointsDifference, boolean sanctionsEnabled,
                 boolean teamTimeoutsEnabled, int teamTimeoutsPerSet, int teamTimeoutDuration,
                 boolean technicalTimeoutsEnabled, int technicalTimeoutDuration,
                 boolean gameIntervalsEnabled, int gameIntervalDuration,
                 int substitutionType, int teamSubstitutionsPerSet,
                 boolean beachCourtSwitchesEnabled, int beachCourtSwitchFrequency, int beachCourtSwitchFrequencyTieBreak, int customConsecutiveServesPerPlayer) {
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

        mSubstitutionType = substitutionType;
        mTeamSubstitutionsPerSet = teamSubstitutionsPerSet;
        checkSubstitutions();

        mBeachCourtSwitchesEnabled = beachCourtSwitchesEnabled;
        mBeachCourtSwitchFrequency = beachCourtSwitchFrequency;
        mBeachCourtSwitchFrequencyTieBreak = beachCourtSwitchFrequencyTieBreak;

        mCustomConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;
    }

    public static Rules defaultUniversalRules() {
        return new Rules(Authentication.VBR_USER_ID, "FIVB indoor 6x6 rules", 0L,
                5, 25, true, 15, true, true, true, 2, 30,
                true, 60, true, 180,
                SINGLE_SUBSTITUTE_TYPE, 6, true, 7, 5, 9999);
    }
    public static Rules officialIndoorRules() {
        return new Rules(Authentication.VBR_USER_ID, "FIVB indoor 6x6 rules", 0L,
                5, 25, true, 15, true, true, true, 2, 30,
                true, 60, true, 180,
                SINGLE_SUBSTITUTE_TYPE, 6, false, 7, 5, 9999);
    }
    public static Rules officialBeachRules() {
        return new Rules(Authentication.VBR_USER_ID, "FIVB beach rules", 0L,
                3, 21, true, 15, true, true, true, 1, 30,
                true, 30, true, 60,
                SINGLE_SUBSTITUTE_TYPE, 6, true, 7, 5, 9999);
    }
    public static Rules defaultIndoor4x4Rules() {
        return new Rules(Authentication.VBR_USER_ID, "Default 4x4 rules", 0L,
                5, 25, true, 15, true, true, true, 2, 30,
                true, 60, true, 180,
                FREE_SUBSTITUTIONS_TYPE, 4, false, 7, 5, 9999);
    }

    public String getUserId() {
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

    public int getSubstitutionType() {
        return mSubstitutionType;
    }

    public int getTeamSubstitutionsPerSet() {
        return mTeamSubstitutionsPerSet;
    }

    public boolean areBeachCourtSwitchesEnabled() {
        return mBeachCourtSwitchesEnabled;
    }

    public int getBeachCourtSwitchFrequency() {
        return mBeachCourtSwitchFrequency;
    }

    public int getBeachCourtSwitchFrequencyTieBreak() {
        return mBeachCourtSwitchFrequencyTieBreak;
    }

    public int getCustomConsecutiveServesPerPlayer() {
        return mCustomConsecutiveServesPerPlayer;
    }

    public void setUserId(String userId) {
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

    public void setSubstitutionType(int substitutionType) {
        this.mSubstitutionType = substitutionType;
        checkSubstitutions();
    }

    public void setTeamSubstitutionsPerSet(int teamSubstitutionsPerSet) {
        mTeamSubstitutionsPerSet = teamSubstitutionsPerSet;
        checkSubstitutions();
    }

    public void setBeachCourtSwitchesEnabled(boolean beachCourtSwitchesEnabled) {
        mBeachCourtSwitchesEnabled = beachCourtSwitchesEnabled;
    }

    public void setBeachCourtSwitchFrequency(int beachCourtSwitchFrequency) {
        mBeachCourtSwitchFrequency = beachCourtSwitchFrequency;
    }

    public void setBeachCourtSwitchFrequencyTieBreak(int beachCourtSwitchFrequencyTieBreak) {
        mBeachCourtSwitchFrequencyTieBreak = beachCourtSwitchFrequencyTieBreak;
    }

    public void setCustomConsecutiveServesPerPlayer(int customConsecutiveServesPerPlayer) {
        mCustomConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;
    }

    public void setAll(Rules rules) {
        mUserId = rules.getUserId();
        mName = rules.getName();
        mDate = rules.getDate();

        mSetsPerGame = rules.getSetsPerGame();
        mPointsPerSet = rules.getPointsPerSet();
        mTieBreakInLastSet = rules.isTieBreakInLastSet();
        mPointsInTieBreak = rules.getPointsInTieBreak();
        mTwoPointsDifference = rules.isTwoPointsDifference();
        mSanctionsEnabled = rules.areSanctionsEnabled();

        mTeamTimeoutsEnabled = rules.areTeamTimeoutsEnabled();
        mTeamTimeoutsPerSet = rules.getTeamTimeoutsPerSet();
        mTeamTimeoutDuration = rules.getTeamTimeoutDuration();

        mTechnicalTimeoutsEnabled = rules.areTechnicalTimeoutsEnabled();
        mTechnicalTimeoutDuration = rules.getTechnicalTimeoutDuration();

        mGameIntervalsEnabled = rules.areGameIntervalsEnabled();
        mGameIntervalDuration = rules.getGameIntervalDuration();

        mSubstitutionType = rules.getSubstitutionType();
        mTeamSubstitutionsPerSet = rules.getTeamSubstitutionsPerSet();

        mBeachCourtSwitchesEnabled = rules.areBeachCourtSwitchesEnabled();
        mBeachCourtSwitchFrequency = rules.getBeachCourtSwitchFrequency();
        mBeachCourtSwitchFrequencyTieBreak = rules.getBeachCourtSwitchFrequencyTieBreak();

        mCustomConsecutiveServesPerPlayer = rules.getCustomConsecutiveServesPerPlayer();
    }

    private void checkSubstitutions() {
        if (SINGLE_SUBSTITUTE_TYPE == mSubstitutionType && mTeamSubstitutionsPerSet > 12) {
            mTeamSubstitutionsPerSet = 12;
        }
    }

    public void printRules() {
        Log.i(Tags.RULES, String.format("setsPerGame: %d", mSetsPerGame));
        Log.i(Tags.RULES, String.format("pointsPerSet: %d", mPointsPerSet));
        Log.i(Tags.RULES, String.format("tieBreakInLastSet: %b", mTieBreakInLastSet));
        Log.i(Tags.RULES, String.format("pointsInTieBreak: %d", mPointsInTieBreak));
        Log.i(Tags.RULES, String.format("twoPointsDifference: %b", mTwoPointsDifference));
        Log.i(Tags.RULES, String.format("sanctions: %b", mSanctionsEnabled));
        Log.i(Tags.RULES, String.format("teamTimeouts: %b", mTeamTimeoutsEnabled));
        Log.i(Tags.RULES, String.format("teamTimeoutsPerSet: %d", mTeamTimeoutsPerSet));
        Log.i(Tags.RULES, String.format("teamTimeoutDuration: %d", mTeamTimeoutDuration));
        Log.i(Tags.RULES, String.format("technicalTimeouts: %b", mTechnicalTimeoutsEnabled));
        Log.i(Tags.RULES, String.format("technicalTimeoutDuration: %d", mTechnicalTimeoutDuration));
        Log.i(Tags.RULES, String.format("gameIntervals: %b", mGameIntervalsEnabled));
        Log.i(Tags.RULES, String.format("gameIntervalDuration: %d", mGameIntervalDuration));
        Log.i(Tags.RULES, String.format("substitutionType: %d", mSubstitutionType));
        Log.i(Tags.RULES, String.format("teamSubstitutionsPerSet: %d", mTeamSubstitutionsPerSet));
        Log.i(Tags.RULES, String.format("beachCourtSwitches: %b", mBeachCourtSwitchesEnabled));
        Log.i(Tags.RULES, String.format("beachCourtSwitchFreq: %d", mBeachCourtSwitchFrequency));
        Log.i(Tags.RULES, String.format("beachCourtSwitchFreqTieBreak: %d", mBeachCourtSwitchFrequencyTieBreak));
        Log.i(Tags.RULES, String.format("customConsecutiveServesPerPlayer: %d", mCustomConsecutiveServesPerPlayer));
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
                    && (this.getSubstitutionType() == other.getSubstitutionType())
                    && (this.getTeamSubstitutionsPerSet() == other.getTeamSubstitutionsPerSet())
                    && (this.areBeachCourtSwitchesEnabled() == other.areBeachCourtSwitchesEnabled())
                    && (this.getBeachCourtSwitchFrequency() == other.getBeachCourtSwitchFrequency())
                    && (this.getBeachCourtSwitchFrequencyTieBreak() == other.getBeachCourtSwitchFrequencyTieBreak())
                    && (this.getCustomConsecutiveServesPerPlayer() == other.getCustomConsecutiveServesPerPlayer());
        }

        return result;
    }
}
