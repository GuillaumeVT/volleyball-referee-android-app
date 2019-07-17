package com.tonkar.volleyballreferee.engine.stored.database;

import androidx.room.TypeConverter;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.team.GenderType;

public class Converters {

    @TypeConverter
    public static GameType toGameType(String gameTypeStr) {
        return GameType.valueOf(gameTypeStr);
    }

    @TypeConverter
    public static String fromGameType(GameType gameType) {
        return gameType.toString();
    }

    @TypeConverter
    public static GameStatus toGameStatus(String gameStatusStr) {
        return GameStatus.valueOf(gameStatusStr);
    }

    @TypeConverter
    public static String fromGameStatus(GameStatus gameStatus) {
        return gameStatus.toString();
    }

    @TypeConverter
    public static GenderType toGenderType(String genderTypeStr) {
        return GenderType.valueOf(genderTypeStr);
    }

    @TypeConverter
    public static String fromGenderType(GenderType genderType) {
        return genderType.toString();
    }

    @TypeConverter
    public static UsageType toUsageType(String usageTypeStr) {
        return UsageType.valueOf(usageTypeStr);
    }

    @TypeConverter
    public static String fromUsageType(UsageType usageType) {
        return usageType.toString();
    }
}
