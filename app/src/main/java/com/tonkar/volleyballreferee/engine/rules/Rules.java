package com.tonkar.volleyballreferee.engine.rules;

import android.util.Log;

import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;

import lombok.ToString;

@ToString
public class Rules extends RulesDto {

    public static final int FIVB_LIMITATION          = 1;
    public static final int ALTERNATIVE_LIMITATION_1 = 2;
    public static final int ALTERNATIVE_LIMITATION_2 = 3;
    public static final int NO_LIMITATION            = 4;

    public static final int WIN_TERMINATION      = 1;
    public static final int ALL_SETS_TERMINATION = 2;

    public static final String DEFAULT_INDOOR_ID     = "efb06d97-264e-425d-b8ca-b499e3b63a95";
    public static final String DEFAULT_INDOOR_4X4_ID = "375dd005-08b6-45f8-a60f-7e04e1e5ba71";
    public static final String DEFAULT_BEACH_ID      = "cceb81c9-2201-4495-8a5e-e289a77e24bf";
    public static final String DEFAULT_SNOW_ID       = "ff03b7e2-f794-4d32-9e6c-a046f75eafa5";
    public static final String DEFAULT_CREATED_BY    = "3fc31b3b-4f2b-47c9-89c8-ad6ead6902ea";

    public static final String DEFAULT_INDOOR_NAME     = "FIVB indoor 6x6 volleyball rules";
    public static final String DEFAULT_INDOOR_4X4_NAME = "Default 4x4 volleyball rules";
    public static final String DEFAULT_BEACH_NAME      = "FIVB beach volleyball rules";
    public static final String DEFAULT_SNOW_NAME       = "FIVB snow volleyball rules";

    public static final int RULES_NAME_MIN_LENGTH = 2;

    // For GSON Deserialization
    public Rules() {
        super();
    }

    public Rules(String id,
                 String createdBy,
                 long createdAt,
                 long updatedAt,
                 String name,
                 GameType kind,
                 int setsPerGame,
                 int pointsPerSet,
                 boolean tieBreakInLastSet,
                 int pointsInTieBreak,
                 boolean twoPointsDifference,
                 boolean sanctions,
                 int matchTermination,
                 boolean teamTimeouts,
                 int teamTimeoutsPerSet,
                 int teamTimeoutDuration,
                 boolean technicalTimeouts,
                 int technicalTimeoutDuration,
                 boolean gameIntervals,
                 int gameIntervalDuration,
                 int substitutionsLimitation,
                 int teamSubstitutionsPerSet,
                 boolean beachCourtSwitches,
                 int beachCourtSwitchFreq,
                 int beachCourtSwitchFreqTieBreak,
                 int customConsecutiveServesPerPlayer) {
        setId(id);
        setCreatedBy(createdBy);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setName(name);
        setKind(kind);
        setSetsPerGame(setsPerGame);
        setPointsPerSet(pointsPerSet);
        setTieBreakInLastSet(tieBreakInLastSet);
        setPointsInTieBreak(pointsInTieBreak);
        setTwoPointsDifference(twoPointsDifference);
        setSanctions(sanctions);
        setMatchTermination(matchTermination);
        setTeamTimeouts(teamTimeouts);
        setTeamTimeoutsPerSet(teamTimeoutsPerSet);
        setTeamTimeoutDuration(teamTimeoutDuration);
        setTechnicalTimeouts(technicalTimeouts);
        setTechnicalTimeoutDuration(technicalTimeoutDuration);
        setGameIntervals(gameIntervals);
        setGameIntervalDuration(gameIntervalDuration);
        setSubstitutionsLimitation(substitutionsLimitation);
        setTeamSubstitutionsPerSet(teamSubstitutionsPerSet);
        setBeachCourtSwitches(beachCourtSwitches);
        setBeachCourtSwitchFreq(beachCourtSwitchFreq);
        setBeachCourtSwitchFreqTieBreak(beachCourtSwitchFreqTieBreak);
        setCustomConsecutiveServesPerPlayer(customConsecutiveServesPerPlayer);
    }

    public static Rules officialIndoorRules() {
        return new Rules(DEFAULT_INDOOR_ID, DEFAULT_CREATED_BY, 0L, 0L, DEFAULT_INDOOR_NAME, GameType.INDOOR, 5, 25, true, 15, true, true,
                         WIN_TERMINATION, true, 2, 30, false, 60, true, 180, FIVB_LIMITATION, 6, false, 0, 0, 9999);
    }

    public static Rules officialBeachRules() {
        return new Rules(DEFAULT_BEACH_ID, DEFAULT_CREATED_BY, 0L, 0L, DEFAULT_BEACH_NAME, GameType.BEACH, 3, 21, true, 15, true, true, WIN_TERMINATION,
                         true, 1, 30, true, 30, true, 60, FIVB_LIMITATION, 0, true, 7, 5, 9999);
    }

    public static Rules defaultIndoor4x4Rules() {
        return new Rules(DEFAULT_INDOOR_4X4_ID, DEFAULT_CREATED_BY, 0L, 0L, DEFAULT_INDOOR_4X4_NAME, GameType.INDOOR_4X4, 5, 25, true, 15, true, true,
                         WIN_TERMINATION, true, 2, 30, true, 60, true, 180, NO_LIMITATION, 4, false, 0, 0, 9999);
    }

    public static Rules officialSnowRules() {
        return new Rules(DEFAULT_SNOW_ID, DEFAULT_CREATED_BY, 0L, 0L, DEFAULT_SNOW_NAME, GameType.SNOW, 3, 15, false, 15, true, true, WIN_TERMINATION,
                         true, 1, 30, false, 0, true, 60, NO_LIMITATION, 2, true, 5, 5, 9999);
    }

    @Override
    public void setSubstitutionsLimitation(int substitutionLimitation) {
        super.setSubstitutionsLimitation(substitutionLimitation);
        checkSubstitutions();
    }

    @Override
    public void setTeamSubstitutionsPerSet(int teamSubstitutionsPerSet) {
        super.setTeamSubstitutionsPerSet(teamSubstitutionsPerSet);
        checkSubstitutions();
    }

    public void setAll(RulesDto rules) {
        setId(rules.getId());
        setCreatedBy(rules.getCreatedBy());
        setCreatedAt(rules.getCreatedAt());
        setUpdatedAt(rules.getUpdatedAt());
        setName(rules.getName());
        setKind(rules.getKind());
        setSetsPerGame(rules.getSetsPerGame());
        setPointsPerSet(rules.getPointsPerSet());
        setTieBreakInLastSet(rules.isTieBreakInLastSet());
        setPointsInTieBreak(rules.getPointsInTieBreak());
        setTwoPointsDifference(rules.isTwoPointsDifference());
        setSanctions(rules.isSanctions());
        setMatchTermination(rules.getMatchTermination());
        setTeamTimeouts(rules.isTeamTimeouts());
        setTeamTimeoutsPerSet(rules.getTeamTimeoutsPerSet());
        setTeamTimeoutDuration(rules.getTeamTimeoutDuration());
        setTechnicalTimeouts(rules.isTechnicalTimeouts());
        setTechnicalTimeoutDuration(rules.getTechnicalTimeoutDuration());
        setGameIntervals(rules.isGameIntervals());
        setGameIntervalDuration(rules.getGameIntervalDuration());
        setSubstitutionsLimitation(rules.getSubstitutionsLimitation());
        setTeamSubstitutionsPerSet(rules.getTeamSubstitutionsPerSet());
        setBeachCourtSwitches(rules.isBeachCourtSwitches());
        setBeachCourtSwitchFreq(rules.getBeachCourtSwitchFreq());
        setBeachCourtSwitchFreqTieBreak(rules.getBeachCourtSwitchFreqTieBreak());
        setCustomConsecutiveServesPerPlayer(rules.getCustomConsecutiveServesPerPlayer());
    }

    private void checkSubstitutions() {
        if (FIVB_LIMITATION == getSubstitutionsLimitation() && getTeamSubstitutionsPerSet() > 12) {
            setTeamSubstitutionsPerSet(12);
        }
    }

    public void printRules() {
        Log.i(Tags.RULES, toString());
    }

    public static RulesSummaryDto getDefaultRules(GameType kind) {
        final Rules rules = switch (kind) {
            case INDOOR -> officialIndoorRules();
            case INDOOR_4X4 -> defaultIndoor4x4Rules();
            case BEACH -> officialBeachRules();
            case SNOW -> officialSnowRules();
        };

        RulesSummaryDto rulesDescription = new RulesSummaryDto();
        rulesDescription.setId(rules.getId());
        rulesDescription.setCreatedBy(rules.getCreatedBy());
        rulesDescription.setCreatedAt(rules.getCreatedAt());
        rulesDescription.setUpdatedAt(rules.getUpdatedAt());
        rulesDescription.setKind(rules.getKind());
        rulesDescription.setName(rules.getName());

        return rulesDescription;
    }

    public static Rules getDefaultRules(String rulesId) {
        return switch (rulesId) {
            case DEFAULT_INDOOR_ID -> officialIndoorRules();
            case DEFAULT_BEACH_ID -> officialBeachRules();
            case DEFAULT_INDOOR_4X4_ID -> defaultIndoor4x4Rules();
            case DEFAULT_SNOW_ID -> officialSnowRules();
            default -> null;
        };
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Rules other) {
            result = super.equals(other);
        }

        return result;
    }
}
