package com.tonkar.volleyballreferee.business.game;

import android.content.SharedPreferences;
import android.util.Log;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.rules.Rules;

public class GameFactory {

    public static IndoorGame createIndoorGame(final String refereeName) {
        Log.i("VBR-Core", "Create indoor game with official rules");
        Rules.OFFICIAL_INDOOR_RULES.printRules();
        IndoorGame game = new IndoorGame(Rules.OFFICIAL_INDOOR_RULES, refereeName);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static IndoorGame createIndoorGameUserRules(final SharedPreferences sharedPreferences, final String refereeName) {
        Log.i("VBR-Core", "Create indoor game with user rules");
        Rules rules = Rules.createRulesFromPref(sharedPreferences, Rules.OFFICIAL_INDOOR_RULES);
        rules.printRules();
        IndoorGame game = new IndoorGame(rules, refereeName);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static BeachGame createBeachGame(final String refereeName) {
        Log.i("VBR-Core", "Create beach game with official rules");
        Rules.OFFICIAL_BEACH_RULES.printRules();
        BeachGame game = new BeachGame(Rules.OFFICIAL_BEACH_RULES, refereeName);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static BeachGame createBeachGameUserRules(final SharedPreferences sharedPreferences, final String refereeName) {
        Log.i("VBR-Core", "Create beach game with user rules");
        Rules rules = Rules.createRulesFromPref(sharedPreferences, Rules.OFFICIAL_BEACH_RULES);
        rules.printRules();
        BeachGame game = new BeachGame(rules, refereeName);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static IndoorGame createPointBasedGame(final String refereeName) {
        Log.i("VBR-Core", "Create score-based game with official rules");
        IndoorGame game = createIndoorGame(refereeName);
        game.setUsageType(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static IndoorGame createPointBasedGameUserRules(final SharedPreferences sharedPreferences, final String refereeName) {
        Log.i("VBR-Core", "Create score-based game with user rules");
        IndoorGame game = createIndoorGameUserRules(sharedPreferences, refereeName);
        game.setUsageType(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static TimeBasedGame createTimeBasedGame(final String refereeName) {
        Log.i("VBR-Core", "Create time-based game");
        TimeBasedGame game = new TimeBasedGame(refereeName);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static Indoor4x4Game createIndoor4x4Game(final String refereeName) {
        Log.i("VBR-Core", "Create indoor 4x4 game with official rules");
        Rules.OFFICIAL_INDOOR_4X4_RULES.printRules();
        Indoor4x4Game game = new Indoor4x4Game(Rules.OFFICIAL_INDOOR_4X4_RULES, refereeName);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

}
