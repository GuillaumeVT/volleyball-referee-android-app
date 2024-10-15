package com.tonkar.volleyballreferee.engine.api.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import lombok.*;

@Getter
@Setter
public class ApiGameSummary {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("id")
    private String     id;
    @ColumnInfo(name = "createdBy")
    @SerializedName("createdBy")
    private String     createdBy;
    @ColumnInfo(name = "createdAt")
    @SerializedName("createdAt")
    private long       createdAt;
    @ColumnInfo(name = "updatedAt")
    @SerializedName("updatedAt")
    private long       updatedAt;
    @ColumnInfo(name = "synced")
    @SerializedName("synced")
    private boolean    synced;
    @ColumnInfo(name = "scheduledAt")
    @SerializedName("scheduledAt")
    private long       scheduledAt;
    @Ignore
    @SerializedName("refereedBy")
    private String     refereedBy;
    @Ignore
    @SerializedName("refereeName")
    private String     refereeName;
    @NonNull
    @ColumnInfo(name = "kind")
    @SerializedName("kind")
    private GameType   kind;
    @NonNull
    @ColumnInfo(name = "gender")
    @SerializedName("gender")
    private GenderType gender;
    @NonNull
    @ColumnInfo(name = "usage")
    @SerializedName("usage")
    private UsageType  usage;
    @Ignore
    @SerializedName("status")
    private GameStatus status;
    @Ignore
    @SerializedName("leagueId")
    private String     leagueId;
    @ColumnInfo(name = "leagueName")
    @SerializedName("leagueName")
    private String     leagueName;
    @ColumnInfo(name = "divisionName")
    @SerializedName("divisionName")
    private String     divisionName;
    @Ignore
    @SerializedName("homeTeamId")
    private String     homeTeamId;
    @NonNull
    @ColumnInfo(name = "homeTeamName")
    @SerializedName("homeTeamName")
    private String     homeTeamName;
    @Ignore
    @SerializedName("guestTeamId")
    private String     guestTeamId;
    @NonNull
    @ColumnInfo(name = "guestTeamName")
    @SerializedName("guestTeamName")
    private String     guestTeamName;
    @ColumnInfo(name = "homeSets")
    @SerializedName("homeSets")
    private int        homeSets;
    @ColumnInfo(name = "guestSets")
    @SerializedName("guestSets")
    private int        guestSets;
    @Ignore
    @SerializedName("rulesId")
    private String     rulesId;
    @Ignore
    @SerializedName("rulesName")
    private String     rulesName;
    @NonNull
    @ColumnInfo(name = "score")
    @SerializedName("score")
    private String     score;
    @Ignore
    @SerializedName("referee1Name")
    private String     referee1Name;
    @Ignore
    @SerializedName("referee2Name")
    private String     referee2Name;
    @Ignore
    @SerializedName("scorerName")
    private String     scorerName;

    public ApiGameSummary() {
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