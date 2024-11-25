package com.tonkar.volleyballreferee.engine.api.model;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class TeamSummaryDto {
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

    @SerializedName("name")
    private String name;

    @ColumnInfo(name = "kind")
    private GameType kind;

    @ColumnInfo(name = "gender")
    private GenderType gender;

    public TeamSummaryDto() {
        id = "";
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        synced = false;
        kind = GameType.INDOOR;
        gender = GenderType.MIXED;
        name = "";
    }
}