package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.tonkar.volleyballreferee.engine.api.model.ApiGameSummary;

import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "games")
@Getter @Setter
public class GameEntity extends ApiGameSummary {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public GameEntity() {
        super();
        content = "";
    }

}
