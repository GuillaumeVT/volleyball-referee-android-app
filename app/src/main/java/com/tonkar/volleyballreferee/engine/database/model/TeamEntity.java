package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.tonkar.volleyballreferee.engine.api.model.ApiTeamSummary;

import lombok.*;

@Entity(tableName = "teams")
@Getter
@Setter
public class TeamEntity extends ApiTeamSummary {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public TeamEntity() {
        super();
        content = "";
    }

}
