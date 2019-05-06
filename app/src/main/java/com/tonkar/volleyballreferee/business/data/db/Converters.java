package com.tonkar.volleyballreferee.business.data.db;

import androidx.room.TypeConverter;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;

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
}
