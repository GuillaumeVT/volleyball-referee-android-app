package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.rules.Rules;

public class GameFactory {

    public static IndoorGame createIndoorGame(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i("VBR-Core", "Create indoor game rules");
        IndoorGame game = new IndoorGame(gameDate, gameSchedule, rules);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static BeachGame createBeachGame(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i("VBR-Core", "Create beach game");
        BeachGame game = new BeachGame(gameDate, gameSchedule, rules);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static IndoorGame createPointBasedGame(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i("VBR-Core", "Create score-based game");
        IndoorGame game = createIndoorGame(gameDate, gameSchedule, rules);
        game.setUsageType(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static TimeBasedGame createTimeBasedGame(final long gameDate, final long gameSchedule) {
        Log.i("VBR-Core", "Create time-based game");
        TimeBasedGame game = new TimeBasedGame(gameDate, gameSchedule);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static Indoor4x4Game createIndoor4x4Game(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i("VBR-Core", "Create indoor 4x4 game");
        Indoor4x4Game game = new Indoor4x4Game(gameDate, gameSchedule, rules);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

}
