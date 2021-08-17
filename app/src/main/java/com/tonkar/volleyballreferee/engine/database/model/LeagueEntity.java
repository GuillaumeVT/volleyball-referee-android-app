package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.tonkar.volleyballreferee.engine.api.model.ApiLeagueSummary;

import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "leagues")
@Getter @Setter
public class LeagueEntity extends ApiLeagueSummary {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public LeagueEntity() {
        super();
        content = "";
    }

}
