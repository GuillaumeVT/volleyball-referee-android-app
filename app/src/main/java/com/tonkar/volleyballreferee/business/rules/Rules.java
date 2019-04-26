package com.tonkar.volleyballreferee.business.rules;

import android.util.Log;

import com.tonkar.volleyballreferee.api.ApiRules;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import lombok.ToString;

@ToString
public class Rules extends ApiRules {

    public static final transient int FIVB_LIMITATION          = 1;
    public static final transient int ALTERNATIVE_LIMITATION_1 = 2;
    public static final transient int ALTERNATIVE_LIMITATION_2 = 3;
    public static final transient int NO_LIMITATION            = 4;

    // For GSON Deserialization
    public Rules() {
        super();
    }

    public Rules(String id, String createdBy, long createdAt, long updatedAt, String name, GameType kind, int setsPerGame, int pointsPerSet, boolean tieBreakInLastSet, int pointsInTieBreak, boolean twoPointsDifference, boolean sanctions,
                 boolean teamTimeouts, int teamTimeoutsPerSet, int teamTimeoutDuration,
                 boolean technicalTimeouts, int technicalTimeoutDuration, boolean gameIntervals, int gameIntervalDuration,
                 int substitutionsLimitation, int teamSubstitutionsPerSet,
                 boolean beachCourtSwitches, int beachCourtSwitchFreq, int beachCourtSwitchFreqTieBreak, int customConsecutiveServesPerPlayer) {
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
        return new Rules("efb06d97-264e-425d-b8ca-b499e3b63a95",
                Authentication.VBR_USER_ID, 0L, 0L, "FIVB indoor 6x6 rules", GameType.INDOOR,
                5, 25, true, 15, true, true, true, 2, 30,
                true, 60, true, 180,
                FIVB_LIMITATION, 6, false, 0, 0, 9999);
    }

    public static Rules officialBeachRules() {
        return new Rules("cceb81c9-2201-4495-8a5e-e289a77e24bf",
                Authentication.VBR_USER_ID, 0L, 0L, "FIVB beach rules", GameType.BEACH,
                3, 21, true, 15, true, true, true, 1, 30,
                true, 30, true, 60,
                FIVB_LIMITATION, 0, true, 7, 5, 9999);
    }

    public static Rules defaultIndoor4x4Rules() {
        return new Rules("375dd005-08b6-45f8-a60f-7e04e1e5ba71",
                Authentication.VBR_USER_ID, 0L, 0L, "Default 4x4 rules", GameType.INDOOR_4X4,
                5, 25, true, 15, true, true, true, 2, 30,
                true, 60, true, 180,
                NO_LIMITATION, 4, false, 0, 0, 9999);
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

    public void setAll(ApiRules rules) {
        setId(rules.getId());
        setCreatedBy(rules.getCreatedBy());
        setCreatedAt(rules.getCreatedAt());
        setUpdatedAt(rules.getUpdatedAt());
        setName(rules.getName());
        setKind(rules.getKind());
        setSetsPerGame(rules.getSetsPerGame());
        setPointsPerSet(rules.getPointsPerSet());
        setTieBreakInLastSet(rules.isTieBreakInLastSet());
        setPointsPerSet(rules.getPointsPerSet());
        setTwoPointsDifference(rules.isTwoPointsDifference());
        setSanctions(rules.isSanctions());
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

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Rules) {
            Rules other = (Rules) obj;
            result = super.equals(other);
        }

        return result;
    }
}
