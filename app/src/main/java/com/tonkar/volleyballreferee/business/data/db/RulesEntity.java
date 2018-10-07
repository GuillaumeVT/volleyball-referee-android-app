package com.tonkar.volleyballreferee.business.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "rules")
public class RulesEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String mName;

    @NonNull
    @ColumnInfo(name = "content")
    private String mContent;

    public RulesEntity() {
        mName = "";
        mContent = "";
    }

    @Ignore
    public RulesEntity(@NonNull String name, @NonNull String content) {
        mName = name;
        mContent = content;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
