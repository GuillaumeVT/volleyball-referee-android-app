package com.tonkar.volleyballreferee.business.game;

import android.content.SharedPreferences;
import android.util.Log;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.rules.Rules;

public class GameFactory {

    public static IndoorGame createIndoorGame() {
        Log.i("VBR-Core", "Create indoor game with official rules");
        Rules.OFFICIAL_INDOOR_RULES.printRules();
        IndoorGame game = new IndoorGame(Rules.OFFICIAL_INDOOR_RULES);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static IndoorGame createIndoorGame(final SharedPreferences sharedPreferences) {
        Log.i("VBR-Core", "Create indoor game with user rules");
        Rules rules = Rules.createRulesFromPref(sharedPreferences, Rules.OFFICIAL_INDOOR_RULES);
        rules.printRules();
        IndoorGame game = new IndoorGame(rules);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static BeachGame createBeachGame() {
        Log.i("VBR-Core", "Create beach game with official rules");
        Rules.OFFICIAL_BEACH_RULES.printRules();
        BeachGame game = new BeachGame(Rules.OFFICIAL_BEACH_RULES);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static BeachGame createBeachGame(final SharedPreferences sharedPreferences) {
        Log.i("VBR-Core", "Create beach game with user rules");
        Rules rules = Rules.createRulesFromPref(sharedPreferences, Rules.OFFICIAL_BEACH_RULES);
        rules.printRules();
        BeachGame game = new BeachGame(rules);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static IndoorGame createPointBasedGame() {
        Log.i("VBR-Core", "Create score-based game with official rules");
        IndoorGame game = createIndoorGame();
        game.setUsageType(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static IndoorGame createPointBasedGame(final SharedPreferences sharedPreferences) {
        Log.i("VBR-Core", "Create score-based game with user rules");
        IndoorGame game = createIndoorGame(sharedPreferences);
        game.setUsageType(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static TimeBasedGame createTimeBasedGame() {
        Log.i("VBR-Core", "Create time-based game");
        TimeBasedGame game = new TimeBasedGame();
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

}
