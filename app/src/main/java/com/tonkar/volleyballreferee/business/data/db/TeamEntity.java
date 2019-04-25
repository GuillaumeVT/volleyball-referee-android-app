package com.tonkar.volleyballreferee.business.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import com.tonkar.volleyballreferee.api.ApiTeamDescription;
import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "teams")
@Getter @Setter
public class TeamEntity extends ApiTeamDescription {

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    public TeamEntity() {
        super();
        content = "";
    }

}
