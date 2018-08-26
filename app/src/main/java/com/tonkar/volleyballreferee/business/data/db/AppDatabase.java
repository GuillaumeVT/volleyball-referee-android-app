package com.tonkar.volleyballreferee.business.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { RulesEntity.class, TeamEntity.class, GameEntity.class, FullGameEntity.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sInstance = null;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context, AppDatabase.class, "vbr-db").allowMainThreadQueries().build();
        }

        return sInstance;
    }

    public abstract RulesDao rulesDao();

    public abstract TeamDao teamDao();

    public abstract GameDao gameDao();

    public abstract FullGameDao fullGameDao();

}
