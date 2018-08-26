package com.tonkar.volleyballreferee.business.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "full_games")
public class FullGameEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "type")
    private String mType;

    @NonNull
    @ColumnInfo(name = "content")
    private String mContent;

    public FullGameEntity() {
        mType = "";
        mContent = "";
    }

    @Ignore
    public FullGameEntity(String type, String content) {
        mType = type;
        mContent = content;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
