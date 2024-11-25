package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import lombok.*;

@Getter
@Setter
public class GameSummaryDto {
    @SerializedName("id")
    private String id;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("createdAt")
    private long createdAt;

    @SerializedName("updatedAt")
    private long updatedAt;

    @SerializedName("synced")
    private boolean synced;

    @SerializedName("scheduledAt")
    private long scheduledAt;

    @SerializedName("refereedBy")
    private String refereedBy;

    @SerializedName("refereeName")
    private String refereeName;

    @SerializedName("kind")
    private GameType kind;

    @SerializedName("gender")
    private GenderType gender;

    @SerializedName("usage")
    private UsageType usage;

    @SerializedName("status")
    private GameStatus status;

    @SerializedName("leagueId")
    private String leagueId;

    @SerializedName("leagueName")
    private String leagueName;

    @SerializedName("divisionName")
    private String divisionName;

    @SerializedName("homeTeamId")
    private String homeTeamId;

    @SerializedName("homeTeamName")
    private String homeTeamName;

    @SerializedName("guestTeamId")
    private String guestTeamId;

    @SerializedName("guestTeamName")
    private String guestTeamName;

    @SerializedName("homeSets")
    private int homeSets;

    @SerializedName("guestSets")
    private int guestSets;

    @SerializedName("rulesId")
    private String rulesId;

    @SerializedName("rulesName")
    private String rulesName;

    @SerializedName("score")
    private String score;

    @SerializedName("referee1Name")
    private String referee1Name;

    @SerializedName("referee2Name")
    private String referee2Name;

    @SerializedName("scorerName")
    private String scorerName;

    public GameSummaryDto() {
        id = "";
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        scheduledAt = 0L;
        synced = false;
        refereedBy = null;
        refereeName = "";
        kind = GameType.INDOOR;
        gender = GenderType.MIXED;
        usage = UsageType.NORMAL;
        status = GameStatus.SCHEDULED;
        leagueId = null;
        leagueName = "";
        divisionName = "";
        homeTeamId = null;
        homeTeamName = "";
        guestTeamId = null;
        guestTeamName = "";
        homeSets = 0;
        guestSets = 0;
        rulesId = null;
        rulesName = "";
        score = "";
        referee1Name = "";
        referee2Name = "";
        scorerName = "";
    }
}