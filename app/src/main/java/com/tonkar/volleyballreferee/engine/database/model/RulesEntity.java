package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.tonkar.volleyballreferee.engine.api.model.ApiRulesSummary;

import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "rules")
@Getter @Setter
public class RulesEntity extends ApiRulesSummary {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public RulesEntity() {
        super();
        content = "";
    }

}
