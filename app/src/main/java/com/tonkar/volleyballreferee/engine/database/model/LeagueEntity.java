package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.UUID;

import lombok.*;

@Entity(tableName = "leagues")
@Getter
@Setter
public class LeagueEntity {
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

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "kind")
    private GameType kind;

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public LeagueEntity() {
        id = UUID.randomUUID().toString();
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        synced = false;
        kind = GameType.INDOOR;
        name = "";
        content = "";
    }
}
