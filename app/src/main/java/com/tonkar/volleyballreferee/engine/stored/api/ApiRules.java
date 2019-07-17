package com.tonkar.volleyballreferee.engine.stored.api;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @EqualsAndHashCode
public class ApiRules {

    @SerializedName("id")
    private String   id;
    @SerializedName("createdBy")
    private String   createdBy;
    @SerializedName("createdAt")
    private long     createdAt;
    @SerializedName("updatedAt")
    private long     updatedAt;
    @SerializedName("name")
    private String   name;
    @SerializedName("kind")
    private GameType kind;
    @SerializedName("setsPerGame")
    private int      setsPerGame;
    @SerializedName("pointsPerSet")
    private int      pointsPerSet;
    @SerializedName("tieBreakInLastSet")
    private boolean  tieBreakInLastSet;
    @SerializedName("pointsInTieBreak")
    private int      pointsInTieBreak;
    @SerializedName("twoPointsDifference")
    private boolean  twoPointsDifference;
    @SerializedName("sanctions")
    private boolean  sanctions;
    @SerializedName("teamTimeouts")
    private boolean  teamTimeouts;
    @SerializedName("teamTimeoutsPerSet")
    private int      teamTimeoutsPerSet;
    @SerializedName("teamTimeoutDuration")
    private int      teamTimeoutDuration;
    @SerializedName("technicalTimeouts")
    private boolean  technicalTimeouts;
    @SerializedName("technicalTimeoutDuration")
    private int      technicalTimeoutDuration;
    @SerializedName("gameIntervals")
    private boolean  gameIntervals;
    @SerializedName("gameIntervalDuration")
    private int      gameIntervalDuration;
    @SerializedName("substitutionsLimitation")
    private int      substitutionsLimitation;
    @SerializedName("teamSubstitutionsPerSet")
    private int      teamSubstitutionsPerSet;
    @SerializedName("beachCourtSwitches")
    private boolean  beachCourtSwitches;
    @SerializedName("beachCourtSwitchFreq")
    private int      beachCourtSwitchFreq;
    @SerializedName("beachCourtSwitchFreqTieBreak")
    private int      beachCourtSwitchFreqTieBreak;
    @SerializedName("customConsecutiveServesPerPlayer")
    private int      customConsecutiveServesPerPlayer;

    public ApiRules() {
        id = UUID.randomUUID().toString();
        createdBy = ApiUserSummary.VBR_USER_ID;
        createdAt = 0L;
        updatedAt = 0L;
        kind = GameType.INDOOR;
        name = "";
        setsPerGame = 0;
        pointsPerSet = 0;
        tieBreakInLastSet = false;
        pointsInTieBreak = 0;
        twoPointsDifference = false;
        sanctions = false;
        teamTimeouts = false;
        teamTimeoutsPerSet = 0;
        teamTimeoutDuration = 0;
        technicalTimeouts = false;
        technicalTimeoutDuration = 0;
        gameIntervals = false;
        gameIntervalDuration = 0;
        substitutionsLimitation = 0;
        teamSubstitutionsPerSet = 0;
        beachCourtSwitches = false;
        beachCourtSwitchFreq = 0;
        beachCourtSwitchFreqTieBreak = 0;
        customConsecutiveServesPerPlayer = 0;
    }

}
