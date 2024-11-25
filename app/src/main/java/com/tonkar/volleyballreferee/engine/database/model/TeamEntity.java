package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import lombok.*;

@Entity(tableName = "teams")
@Getter
@Setter
public class TeamEntity {
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
    @ColumnInfo(name = "gender")
    private GenderType gender;

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public TeamEntity() {
        id = "";
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        synced = false;
        kind = GameType.INDOOR;
        gender = GenderType.MIXED;
        name = "";
        content = "";
    }
}
