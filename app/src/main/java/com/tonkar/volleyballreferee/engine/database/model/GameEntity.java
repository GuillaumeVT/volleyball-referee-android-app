package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import lombok.*;

@Entity(tableName = "games")
@Getter
@Setter
public class GameEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "createdBy")
    private String createdBy;

    @ColumnInfo(name = "createdAt")
    private long createdAt;

    @ColumnInfo(name = "updatedAt")
    private long updatedAt;

    @ColumnInfo(name = "synced")
    private boolean synced;

    @ColumnInfo(name = "scheduledAt")
    private long scheduledAt;

    @Ignore
    @SerializedName("refereedBy")
    private String refereedBy;

    @NonNull
    @ColumnInfo(name = "kind")
    private GameType kind;

    @NonNull
    @ColumnInfo(name = "gender")
    private GenderType gender;

    @NonNull
    @ColumnInfo(name = "usage")
    private UsageType usage;

    @ColumnInfo(name = "leagueName")
    private String leagueName;

    @ColumnInfo(name = "divisionName")
    private String divisionName;

    @NonNull
    @ColumnInfo(name = "homeTeamName")
    private String homeTeamName;

    @NonNull
    @ColumnInfo(name = "guestTeamName")
    private String guestTeamName;

    @ColumnInfo(name = "homeSets")
    private int homeSets;

    @ColumnInfo(name = "guestSets")
    private int guestSets;

    @NonNull
    @ColumnInfo(name = "score")
    private String score;

    @NonNull
    @ColumnInfo(name = "content")
    private String content;
}
