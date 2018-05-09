package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.data.UserId;

public class GameFactory {

    public static IndoorGame createIndoorGame(final String refereeName, final UserId userId) {
        Log.i("VBR-Core", "Create indoor game rules");
        IndoorGame game = new IndoorGame(refereeName, userId);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static BeachGame createBeachGame(final String refereeName, final UserId userId) {
        Log.i("VBR-Core", "Create beach game");
        BeachGame game = new BeachGame(refereeName, userId);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static IndoorGame createPointBasedGame(final String refereeName, final UserId userId) {
        Log.i("VBR-Core", "Create score-based game");
        IndoorGame game = createIndoorGame(refereeName, userId);
        game.setUsageType(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static TimeBasedGame createTimeBasedGame(final String refereeName, final UserId userId) {
        Log.i("VBR-Core", "Create time-based game");
        TimeBasedGame game = new TimeBasedGame(refereeName, userId);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

    public static Indoor4x4Game createIndoor4x4Game(final String refereeName, final UserId userId) {
        Log.i("VBR-Core", "Create indoor 4x4 game");
        Indoor4x4Game game = new Indoor4x4Game(refereeName, userId);
        ServicesProvider.getInstance().initGameService(game);
        return game;
    }

}
