package com.tonkar.volleyballreferee.business.data.db;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "syncs")
public class SyncEntity {

    @Ignore
    public static final transient String GAME_ENTITY = "game";
    @Ignore
    public static final transient String TEAM_ENTITY = "team";
    @Ignore
    public static final transient String RULES_ENTITY = "rules";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "item")
    private String mItem;

    @NonNull
    @ColumnInfo(name = "type")
    private String mType;

    public SyncEntity() {
        mItem = "";
        mType = "";
    }

    @Ignore
    public SyncEntity(@NonNull String item, @NonNull String type) {
        mItem = item;
        mType = type;
    }

    @Ignore
    public static SyncEntity createGameSyncEntity(Long date) {
        return new SyncEntity(createGameItem(date), GAME_ENTITY);
    }

    @Ignore
    public static SyncEntity createTeamSyncEntity(String name, String gender, String kind) {
        return new SyncEntity(createTeamItem(name, gender, kind), TEAM_ENTITY);
    }

    @Ignore
    public static SyncEntity createRulesSyncEntity(String name) {
        return new SyncEntity(createRulesItem(name), RULES_ENTITY);
    }

    @Ignore
    public static String createGameItem(Long date) {
        return date.toString();
    }

    @Ignore
    public static String createTeamItem(String name, String gender, String kind) {
        return String.format(Locale.getDefault(), "%s_%s_%s", name, gender, kind);
    }

    @Ignore
    public static String createRulesItem(String name) {
        return name;
    }

    public String getItem() {
        return mItem;
    }

    public void setItem(@NonNull String item) {
        this.mItem = item;
    }

    @NonNull
    public String getType() {
        return mType;
    }

    public void setType(@NonNull String type) {
        this.mType = type;
    }
}
