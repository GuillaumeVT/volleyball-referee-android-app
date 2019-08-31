package com.tonkar.volleyballreferee.engine.stored.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "full_games")
@AllArgsConstructor @Getter @Setter
public class FullGameEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "type")
    private String type;

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public FullGameEntity() {
        this.type = "";
        this.content = "";
    }

}
