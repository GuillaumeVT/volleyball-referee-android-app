package com.tonkar.volleyballreferee.engine.api.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class ApiLeagueSummary {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("id")
    private String   id;
    @ColumnInfo(name = "createdBy")
    @SerializedName("createdBy")
    private String   createdBy;
    @ColumnInfo(name = "createdAt")
    @SerializedName("createdAt")
    private long     createdAt;
    @ColumnInfo(name = "updatedAt")
    @SerializedName("updatedAt")
    private long     updatedAt;
    @ColumnInfo(name = "synced")
    @SerializedName("synced")
    private boolean  synced;
    @NonNull
    @ColumnInfo(name = "name")
    @SerializedName("name")
    private String   name;
    @NonNull
    @ColumnInfo(name = "kind")
    @SerializedName("kind")
    private GameType kind;

    public ApiLeagueSummary() {
        id = UUID.randomUUID().toString();
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        synced = false;
        kind = GameType.INDOOR;
        name = "";
    }

    public void setAll(ApiLeagueSummary league) {
        if (league != null) {
            setId(league.getId());
            setCreatedBy(league.getCreatedBy());
            setCreatedAt(league.getCreatedAt());
            setUpdatedAt(league.getUpdatedAt());
            setKind(league.getKind());
            setName(league.getName());
        }
    }
}
