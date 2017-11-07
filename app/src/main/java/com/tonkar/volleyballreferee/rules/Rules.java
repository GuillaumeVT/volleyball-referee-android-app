package com.tonkar.volleyballreferee.rules;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.Serializable;

public class Rules implements Serializable {

    private final int     mSetsPerGame;
    private final int     mPointsPerSet;
    private final boolean mTieBreakInLastSet;
    private final boolean mTeamTimeoutsEnabled;
    private final int     mTeamTimeoutsPerSet;
    private final int     mTeamTimeoutDuration;
    private final boolean mTechnicalTimeoutsEnabled;
    private final int     mTechnicalTimeoutDuration;
    private final boolean mGameIntervalsEnabled;
    private final int     mGameIntervalDuration;
    private final boolean mTeamOf6Players;
    private final boolean mChangeSidesEvery7Points;

    public Rules(final int setsPerGame, final int pointsPerSet, final boolean tieBreakInLastSet,
                 final boolean teamTimeoutsEnabled, final int teamTimeoutsPerSet, final int teamTimeoutDuration,
                 final boolean technicalTimeoutsEnabled, final int technicalTimeoutDuration,
                 final boolean gameIntervalsEnabled, final int gameIntervalDuration,
                 final boolean teamOf6Players, final boolean changeSidesEvery7Points) {
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

        mTeamOf6Players = teamOf6Players;
        mChangeSidesEvery7Points = changeSidesEvery7Points;
    }

    public static final Rules OFFICIAL_INDOOR_RULES = new Rules(5, 25, true, true, 2, 30, true, 60, true, 180, true, false);
    public static final Rules OFFICIAL_BEACH_RULES  = new Rules(3, 21, true, true, 1, 30, true, 30, true, 60, false, true);

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

        boolean teamOf6Players = sharedPreferences.getBoolean("pref_players_number", defaultRules.isTeamOf6Players());

        boolean changeSidesEvery7Points = sharedPreferences.getBoolean("pref_change_side_every_7_points", defaultRules.isChangeSidesEvery7Points());

        return new Rules(setsPerGame, pointsPerSet, tieBreakInLastSet, teamTimeoutsEnabled, teamTimeoutsPerSet, teamTimeoutDuration,
                technicalTimeoutsEnabled, technicalTimeoutDuration, gameIntervalsEnabled, gameIntervalDuration, teamOf6Players, changeSidesEvery7Points);
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

    public boolean isTeamOf6Players() {
        return mTeamOf6Players;
    }

    public boolean isChangeSidesEvery7Points() { return mChangeSidesEvery7Points; }

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
        Log.i("VBR-Rules", String.format("pref_players_number: %b", mTeamOf6Players));
        Log.i("VBR-Rules", String.format("pref_change_side_every_7_points: %b", mChangeSidesEvery7Points));
    }
}
