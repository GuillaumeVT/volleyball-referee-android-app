package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.tonkar.volleyballreferee.engine.api.model.ApiRulesSummary;

import lombok.*;

@Entity(tableName = "rules")
@Getter
@Setter
public class RulesEntity extends ApiRulesSummary {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public RulesEntity() {
        super();
        content = "";
    }

}
