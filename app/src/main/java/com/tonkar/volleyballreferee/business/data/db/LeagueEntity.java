package com.tonkar.volleyballreferee.business.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import com.tonkar.volleyballreferee.api.ApiLeagueDescription;
import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "leagues")
@Getter @Setter
public class LeagueEntity extends ApiLeagueDescription {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public LeagueEntity() {
        super();
        content = "";
    }

}
