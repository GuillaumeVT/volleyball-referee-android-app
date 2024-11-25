package com.tonkar.volleyballreferee.engine.database;

import androidx.room.TypeConverter;

import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.GenderType;

public class DatabaseConverters {

    @TypeConverter
    public static GameType toGameType(String gameTypeStr) {
        return GameType.valueOf(gameTypeStr);
    }

    @TypeConverter
    public static String fromGameType(GameType gameType) {
        return gameType.toString();
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
