package com.tonkar.volleyballreferee.business.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = { RulesEntity.class, TeamEntity.class, GameEntity.class, FullGameEntity.class, LeagueEntity.class }, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sInstance = null;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context, AppDatabase.class, "vbr-db").addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3).allowMainThreadQueries().build();
        }

        return sInstance;
    }

    public abstract RulesDao rulesDao();

    public abstract TeamDao teamDao();

    public abstract GameDao gameDao();

    public abstract FullGameDao fullGameDao();

    public abstract LeagueDao leagueDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `syncs` (`item` TEXT NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`item`))");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE `syncs`");
            database.execSQL("DROP TABLE `full_games`");
            database.execSQL("DROP TABLE `games`");
            database.execSQL("DROP TABLE `rules`");
            database.execSQL("DROP TABLE `teams`");
            database.execSQL("CREATE TABLE `leagues` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL("CREATE TABLE `full_games` (`type` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`type`))");
            database.execSQL("CREATE TABLE `games` (`id` TEXT NOT NULL, `scheduledAt` INTEGER NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL("CREATE TABLE `teams` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `gender` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL("CREATE TABLE `rules` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
        }
    };

}
