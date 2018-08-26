package com.tonkar.volleyballreferee.business.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "games")
public class GameEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    private Long mDate;

    @NonNull
    @ColumnInfo(name = "league")
    private String mLeague;

    @NonNull
    @ColumnInfo(name = "division")
    private String mDivision;

    @NonNull
    @ColumnInfo(name = "content")
    private String mContent;

    public GameEntity() {
        mDate = 0L;
        mLeague = "";
        mDivision = "";
        mContent = "";
    }

    @Ignore
    public GameEntity(Long date, String league, String division, String content) {
        mDate = date;
        mLeague = league;
        mDivision = division;
        mContent = content;
    }

    public Long getDate() {
        return mDate;
    }

    public void setDate(Long date) {
        this.mDate = date;
    }

    public String getLeague() {
        return mLeague;
    }

    public void setLeague(String league) {
        this.mLeague = league;
    }

    public String getDivision() {
        return mDivision;
    }

    public void setDivision(String division) {
        this.mDivision = division;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
