package com.tonkar.volleyballreferee.business.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "teams", primaryKeys = {"name", "gender", "kind"})
public class TeamEntity {

    @NonNull
    @ColumnInfo(name = "name")
    private String mName;

    @NonNull
    @ColumnInfo(name = "gender")
    private String mGender;

    @NonNull
    @ColumnInfo(name = "kind")
    private String mKind;

    @NonNull
    @ColumnInfo(name = "content")
    private String mContent;

    public TeamEntity() {
        mName = "";
        mGender = "";
        mKind = "";
        mContent = "";
    }

    @Ignore
    public TeamEntity(String name, String gender, String kind, String content) {
        mName = name;
        mGender = gender;
        mKind = kind;
        mContent = content;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        this.mGender = gender;
    }

    public String getKind() {
        return mKind;
    }

    public void setKind(String kind) {
        this.mKind = kind;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
