package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.business.rules.Rules;

public class GameFactory {

    public static IndoorGame createIndoorGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        Log.i(Tags.FACTORY, "Create indoor game rules");
        return new IndoorGame(id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    public static BeachGame createBeachGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        Log.i(Tags.FACTORY, "Create beach game");
        return new BeachGame(id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    public static IndoorGame createPointBasedGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        Log.i(Tags.FACTORY, "Create score-based game");
        IndoorGame game = createIndoorGame(id, createdBy, refereeName, createdAt, scheduledAt, rules);
        game.setUsage(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static TimeBasedGame createTimeBasedGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt) {
        Log.i(Tags.FACTORY, "Create time-based game");
        return new TimeBasedGame(id, createdBy, refereeName, createdAt, scheduledAt);
    }

    public static Indoor4x4Game createIndoor4x4Game(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        Log.i(Tags.FACTORY, "Create indoor 4x4 game");
        return new Indoor4x4Game(id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    public static GameService createGame(StoredGameService storedGameService) {
        Log.i(Tags.FACTORY, "Create game from web");
        GameService gameService = null;

        switch (storedGameService.getKind()) {
            case INDOOR:
                IndoorGame indoorGame = createIndoorGame(storedGameService.getId(), storedGameService.getCreatedBy(), storedGameService.getRefereeName(),
                        storedGameService.getCreatedAt(), storedGameService.getScheduledAt(), storedGameService.getRules());
                indoorGame.setUsage(storedGameService.getUsage());
                indoorGame.setIndexed(storedGameService.isIndexed());
                indoorGame.getLeague().setAll(storedGameService.getLeague());
                indoorGame.restoreTeams(storedGameService);
                gameService = indoorGame;
                break;
            case BEACH:
                BeachGame beachGame = createBeachGame(storedGameService.getId(), storedGameService.getCreatedBy(), storedGameService.getRefereeName(),
                        storedGameService.getCreatedAt(), storedGameService.getScheduledAt(), storedGameService.getRules());
                beachGame.setIndexed(storedGameService.isIndexed());
                beachGame.getLeague().setAll(storedGameService.getLeague());
                beachGame.restoreTeams(storedGameService);
                gameService = beachGame;
                break;
            case INDOOR_4X4:
                Indoor4x4Game indoor4x4Game = createIndoor4x4Game(storedGameService.getId(), storedGameService.getCreatedBy(), storedGameService.getRefereeName(),
                        storedGameService.getCreatedAt(), storedGameService.getScheduledAt(), storedGameService.getRules());
                indoor4x4Game.setUsage(storedGameService.getUsage());
                indoor4x4Game.setIndexed(storedGameService.isIndexed());
                indoor4x4Game.getLeague().setAll(storedGameService.getLeague());
                indoor4x4Game.restoreTeams(storedGameService);
                gameService = indoor4x4Game;
                break;
        }

        return gameService;
    }
}
