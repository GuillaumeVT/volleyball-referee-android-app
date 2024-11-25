package com.tonkar.volleyballreferee.engine.database;

import android.content.Context;

import androidx.room.*;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tonkar.volleyballreferee.engine.database.model.*;

import java.util.concurrent.*;

@Database(entities = { RulesEntity.class, TeamEntity.class, GameEntity.class, FullGameEntity.class, LeagueEntity.class, FriendEntity.class
}, version = 5)
@TypeConverters({ DatabaseConverters.class })
public abstract class VbrDatabase extends RoomDatabase {

    private static final int NUMBER_OF_THREADS = 4;

    private static VbrDatabase sInstance = null;

    static final ExecutorService sDatabaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static VbrDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (VbrDatabase.class) {
                sInstance = Room
                        .databaseBuilder(context, VbrDatabase.class, "vbr-db")
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_3_4)
                        .addMigrations(MIGRATION_4_5)
                        .allowMainThreadQueries()
                        .build();
            }
        }

        return sInstance;
    }

    public abstract RulesDao rulesDao();

    public abstract TeamDao teamDao();

    public abstract GameDao gameDao();

    public abstract FullGameDao fullGameDao();

    public abstract LeagueDao leagueDao();

    public abstract FriendDao friendDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `syncs` (`item` TEXT NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`item`))");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS `syncs`");
            database.execSQL("DROP TABLE IF EXISTS `full_games`");
            database.execSQL("DROP TABLE IF EXISTS `games`");
            database.execSQL("DROP TABLE IF EXISTS `rules`");
            database.execSQL("DROP TABLE IF EXISTS `teams`");
            database.execSQL("DROP TABLE IF EXISTS `syncs`");

            database.execSQL("CREATE TABLE `friends` (`id` TEXT NOT NULL, `pseudo` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL(
                    "CREATE TABLE `leagues` (`id` TEXT NOT NULL, `createdBy` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL("CREATE TABLE `full_games` (`type` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`type`))");
            database.execSQL(
                    "CREATE TABLE `games` (`id` TEXT NOT NULL, `createdBy` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `scheduledAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `kind` TEXT NOT NULL, `gender` TEXT NOT NULL, `usage` TEXT NOT NULL, `public` INTEGER NOT NULL, `leagueName` TEXT, `divisionName` TEXT, `homeTeamName` TEXT NOT NULL, `guestTeamName` TEXT NOT NULL, `homeSets` INTEGER NOT NULL, `guestSets` INTEGER NOT NULL, `score` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL(
                    "CREATE TABLE `teams` (`id` TEXT NOT NULL, `createdBy` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `gender` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL(
                    "CREATE TABLE `rules` (`id` TEXT NOT NULL, `createdBy` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DELETE FROM `leagues` WHERE `kind` = 'TIME'");
            database.execSQL("DELETE FROM `games` WHERE `kind` = 'TIME'");
            database.execSQL("DELETE FROM `teams` WHERE `kind` = 'TIME'");
            database.execSQL("DELETE FROM `rules` WHERE `kind` = 'TIME'");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `games` RENAME TO `games_old`");
            database.execSQL("ALTER TABLE `leagues` RENAME TO `leagues_old`");
            database.execSQL("ALTER TABLE `rules` RENAME TO `rules_old`");
            database.execSQL("ALTER TABLE `teams` RENAME TO `teams_old`");

            database.execSQL(
                    "CREATE TABLE `games` (`id` TEXT NOT NULL, `createdBy` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `scheduledAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `kind` TEXT NOT NULL, `gender` TEXT NOT NULL, `usage` TEXT NOT NULL, `leagueName` TEXT, `divisionName` TEXT, `homeTeamName` TEXT NOT NULL, `guestTeamName` TEXT NOT NULL, `homeSets` INTEGER NOT NULL, `guestSets` INTEGER NOT NULL, `score` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL(
                    "CREATE TABLE `leagues` (`id` TEXT NOT NULL, `createdBy` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL(
                    "CREATE TABLE `rules` (`id` TEXT NOT NULL, `createdBy` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL(
                    "CREATE TABLE `teams` (`id` TEXT NOT NULL, `createdBy` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `name` TEXT NOT NULL, `kind` TEXT NOT NULL, `gender` TEXT NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))");

            database.execSQL(
                    "INSERT INTO `games` SELECT `id`, `createdBy`, `createdAt`, `updatedAt`, `scheduledAt`, `synced`, `kind`, `gender`, `usage`, `leagueName`, `divisionName`, `homeTeamName`, `guestTeamName`, `homeSets`, `guestSets`, `score`, `content` FROM `games_old`");
            database.execSQL(
                    "INSERT INTO `leagues` SELECT `id`, `createdBy`, `createdAt`, `updatedAt`, `synced`, `name`, `kind`, `content` FROM `leagues_old`");
            database.execSQL(
                    "INSERT INTO `rules` SELECT `id`, `createdBy`, `createdAt`, `updatedAt`, `synced`, `name`, `kind`, `content` FROM `rules_old`");
            database.execSQL(
                    "INSERT INTO `teams` SELECT `id`, `createdBy`, `createdAt`, `updatedAt`, `synced`, `name`, `kind`, `gender`, `content` FROM `teams_old`");

            database.execSQL("UPDATE `games` SET `createdBy` = NULL");
            database.execSQL("UPDATE `leagues` SET `createdBy` = NULL");
            database.execSQL("UPDATE `rules` SET `createdBy` = NULL");
            database.execSQL("UPDATE `teams` SET `createdBy` = NULL");

            database.execSQL("DROP TABLE IF EXISTS `games_old`");
            database.execSQL("DROP TABLE IF EXISTS `leagues_old`");
            database.execSQL("DROP TABLE IF EXISTS `rules_old`");
            database.execSQL("DROP TABLE IF EXISTS `teams_old`");
        }
    };
}
