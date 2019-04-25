package com.tonkar.volleyballreferee.business.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import com.tonkar.volleyballreferee.api.ApiRulesDescription;
import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "rules")
@Getter @Setter
public class RulesEntity extends ApiRulesDescription {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public RulesEntity() {
        super();
        content = "";
    }

}
