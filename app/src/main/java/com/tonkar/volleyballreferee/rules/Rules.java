package com.tonkar.volleyballreferee.rules;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class Rules {

    @SerializedName("pref_sets_per_game")
    private final int     mSetsPerGame;
    @SerializedName("pref_points_per_set")
    private final int     mPointsPerSet;
    @SerializedName("pref_tie_break")
    private final boolean mTieBreakInLastSet;
    @SerializedName("pref_team_timeouts")
    private final boolean mTeamTimeoutsEnabled;
    @SerializedName("pref_team_timeouts_per_set")
    private final int     mTeamTimeoutsPerSet;
    @SerializedName("pref_team_timeout_duration")
    private final int     mTeamTimeoutDuration;
    @SerializedName("pref_technical_timeouts")
    private final boolean mTechnicalTimeoutsEnabled;
    @SerializedName("pref_technical_timeout_duration")
    private final int     mTechnicalTimeoutDuration;
    @SerializedName("pref_game_intervals")
    private final boolean mGameIntervalsEnabled;
    @SerializedName("pref_game_intervals_duration")
    private final int     mGameIntervalDuration;
    @SerializedName("pref_team_substitutions_per_set")
    private final int     mTeamSubstitutionsPerSet;
    @SerializedName("pref_change_side_every_7_points")
    private final boolean mChangeSidesEvery7Points;
    @SerializedName("pref_consecutive_serves_per_player")
    private final int     mCustomConsecutiveServesPerPlayer;

    public Rules(final int setsPerGame, final int pointsPerSet, final boolean tieBreakInLastSet,
                 final boolean teamTimeoutsEnabled, final int teamTimeoutsPerSet, final int teamTimeoutDuration,
                 final boolean technicalTimeoutsEnabled, final int technicalTimeoutDuration,
                 final boolean gameIntervalsEnabled, final int gameIntervalDuration,
                 final int teamSubstitutionsPerSet, final boolean changeSidesEvery7Points, final int customConsecutiveServesPerPlayer) {
        mSetsPerGame = setsPerGame;
        mPointsPerSet = pointsPerSet;
        mTieBreakInLastSet = tieBreakInLastSet;

        mTeamTimeoutsEnabled = teamTimeoutsEnabled;
        mTeamTimeoutsPerSet = teamTimeoutsPerSet;
        mTeamTimeoutDuration = teamTimeoutDuration;

        mTechnicalTimeoutsEnabled = technicalTimeoutsEnabled;
        mTechnicalTimeoutDuration = technicalTimeoutDuration;

        mGameIntervalsEnabled = gameIntervalsEnabled;
        mGameIntervalDuration = gameIntervalDuration;

        mTeamSubstitutionsPerSet = teamSubstitutionsPerSet;
        mChangeSidesEvery7Points = changeSidesEvery7Points;

        mCustomConsecutiveServesPerPlayer = customConsecutiveServesPerPlayer;
    }

    public static final Rules OFFICIAL_INDOOR_RULES = new Rules(5, 25, true, true, 2, 30,
            true, 60, true, 180, 6, false, 9999);
    public static final Rules OFFICIAL_BEACH_RULES  = new Rules(3, 21, true, true, 1, 30,
            true, 30, true, 60, 0, true, 9999);

    public static Rules createRulesFromPref(final SharedPreferences sharedPreferences, final Rules defaultRules) {
        int setsPerGame = getInt(sharedPreferences, "pref_sets_per_game", defaultRules.getSetsPerGame());
        int pointsPerSet = getInt(sharedPreferences,"pref_points_per_set", defaultRules.getPointsPerSet());
        boolean tieBreakInLastSet = sharedPreferences.getBoolean("pref_tie_break", defaultRules.isTieBreakInLastSet());

        boolean teamTimeoutsEnabled = sharedPreferences.getBoolean("pref_team_timeouts", defaultRules.areTeamTimeoutsEnabled());
        int teamTimeoutsPerSet = getInt(sharedPreferences,"pref_team_timeouts_per_set", defaultRules.getTeamTimeoutsPerSet());
        int teamTimeoutDuration = getInt(sharedPreferences,"pref_team_timeout_duration", defaultRules.getTeamTimeoutDuration());

        boolean technicalTimeoutsEnabled = sharedPreferences.getBoolean("pref_technical_timeouts", defaultRules.areTechnicalTimeoutsEnabled());
        int technicalTimeoutDuration = getInt(sharedPreferences,"pref_technical_timeout_duration", defaultRules.getTechnicalTimeoutDuration());

        boolean gameIntervalsEnabled = sharedPreferences.getBoolean("pref_game_intervals", defaultRules.areGameIntervalsEnabled());
        int gameIntervalDuration = getInt(sharedPreferences,"pref_game_intervals_duration", defaultRules.getGameIntervalDuration());

        int teamSubstitutionsPerSet = getInt(sharedPreferences,"pref_team_substitutions_per_set", defaultRules.getTeamSubstitutionsPerSet());

        boolean changeSidesEvery7Points = sharedPreferences.getBoolean("pref_change_side_every_7_points", defaultRules.isChangeSidesEvery7Points());

        int customConsecutiveServesPerPlayer = getInt(sharedPreferences,"pref_consecutive_serves_per_player", defaultRules.getCustomConsecutiveServesPerPlayer());

        return new Rules(setsPerGame, pointsPerSet, tieBreakInLastSet, teamTimeoutsEnabled, teamTimeoutsPerSet, teamTimeoutDuration,
                technicalTimeoutsEnabled, technicalTimeoutDuration, gameIntervalsEnabled, gameIntervalDuration, teamSubstitutionsPerSet, changeSidesEvery7Points, customConsecutiveServesPerPlayer);
    }

    private static int getInt(final SharedPreferences sharedPreferences, final String key, final int defaultValue) {
        return Integer.parseInt(sharedPreferences.getString(key, String.valueOf(defaultValue)));
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

    public boolean areTeamTimeoutsEnabled() { return mTeamTimeoutsEnabled; }

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

    public boolean isChangeSidesEvery7Points() { return mChangeSidesEvery7Points; }

    public int getCustomConsecutiveServesPerPlayer() {
        return mCustomConsecutiveServesPerPlayer;
    }

    public void printRules() {
        Log.i("VBR-Rules", String.format("pref_sets_per_game: %d", mSetsPerGame));
        Log.i("VBR-Rules", String.format("pref_points_per_set: %d", mPointsPerSet));
        Log.i("VBR-Rules", String.format("pref_tie_break: %b", mTieBreakInLastSet));
        Log.i("VBR-Rules", String.format("pref_team_timeouts: %b", mTeamTimeoutsEnabled));
        Log.i("VBR-Rules", String.format("pref_team_timeouts_per_set: %d", mTeamTimeoutsPerSet));
        Log.i("VBR-Rules", String.format("pref_team_timeout_duration: %d", mTeamTimeoutDuration));
        Log.i("VBR-Rules", String.format("pref_technical_timeouts: %b", mTechnicalTimeoutsEnabled));
        Log.i("VBR-Rules", String.format("pref_technical_timeout_duration: %d", mTechnicalTimeoutDuration));
        Log.i("VBR-Rules", String.format("pref_game_intervals: %b", mGameIntervalsEnabled));
        Log.i("VBR-Rules", String.format("pref_game_intervals_duration: %d", mGameIntervalDuration));
        Log.i("VBR-Rules", String.format("pref_team_substitutions_per_set: %d", mTeamSubstitutionsPerSet));
        Log.i("VBR-Rules", String.format("pref_change_side_every_7_points: %b", mChangeSidesEvery7Points));
        Log.i("VBR-Rules", String.format("pref_consecutive_serves_per_player: %d", mCustomConsecutiveServesPerPlayer));
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Rules) {
            Rules other = (Rules) obj;
            result = (this.getSetsPerGame() == other.getSetsPerGame())
                    && (this.getPointsPerSet() == other.getPointsPerSet())
                    && (this.isTieBreakInLastSet() == other.isTieBreakInLastSet())
                    && (this.areTeamTimeoutsEnabled() == other.areTeamTimeoutsEnabled())
                    && (this.getTeamTimeoutsPerSet() == other.getTeamTimeoutsPerSet())
                    && (this.getTeamTimeoutDuration() == other.getTeamTimeoutDuration())
                    && (this.areTechnicalTimeoutsEnabled() == other.areTechnicalTimeoutsEnabled())
                    && (this.getTechnicalTimeoutDuration() == other.getTechnicalTimeoutDuration())
                    && (this.areGameIntervalsEnabled() == other.areGameIntervalsEnabled())
                    && (this.getGameIntervalDuration() == other.getGameIntervalDuration())
                    && (this.getTeamSubstitutionsPerSet() == other.getTeamSubstitutionsPerSet())
                    && (this.isChangeSidesEvery7Points() == other.isChangeSidesEvery7Points())
                    && (this.getCustomConsecutiveServesPerPlayer() == other.getCustomConsecutiveServesPerPlayer());
        }

        return result;
    }
}
