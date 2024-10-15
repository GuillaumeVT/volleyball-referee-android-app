package com.tonkar.volleyballreferee.engine.game;

import android.util.Log;

import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;

public class GameFactory {

    public static IndoorGame createIndoorGame(String id,
                                              String createdBy,
                                              String refereeName,
                                              long createdAt,
                                              long scheduledAt,
                                              Rules rules) {
        Log.i(Tags.FACTORY, "Create indoor game rules");
        return new IndoorGame(id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    public static BeachGame createBeachGame(String id,
                                            String createdBy,
                                            String refereeName,
                                            long createdAt,
                                            long scheduledAt,
                                            Rules rules) {
        Log.i(Tags.FACTORY, "Create beach game");
        return new BeachGame(id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    public static IndoorGame createPointBasedGame(String id,
                                                  String createdBy,
                                                  String refereeName,
                                                  long createdAt,
                                                  long scheduledAt,
                                                  Rules rules) {
        Log.i(Tags.FACTORY, "Create score-based game");
        IndoorGame game = createIndoorGame(id, createdBy, refereeName, createdAt, scheduledAt, rules);
        game.setUsage(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static Indoor4x4Game createIndoor4x4Game(String id,
                                                    String createdBy,
                                                    String refereeName,
                                                    long createdAt,
                                                    long scheduledAt,
                                                    Rules rules) {
        Log.i(Tags.FACTORY, "Create indoor 4x4 game");
        return new Indoor4x4Game(id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    public static SnowGame createSnowGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        Log.i(Tags.FACTORY, "Create snow game");
        return new SnowGame(id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    public static IGame createGame(IStoredGame storedGame) {
        Log.i(Tags.FACTORY, "Create game from web");
        IGame game = null;

        switch (storedGame.getKind()) {
            case INDOOR -> {
                IndoorGame indoorGame = createIndoorGame(storedGame.getId(), storedGame.getCreatedBy(), storedGame.getRefereeName(),
                                                         storedGame.getCreatedAt(), storedGame.getScheduledAt(), storedGame.getRules());
                indoorGame.setUsage(storedGame.getUsage());
                indoorGame.getLeague().setAll(storedGame.getLeague());
                indoorGame.restoreTeams(storedGame);
                game = indoorGame;
            }
            case BEACH -> {
                BeachGame beachGame = createBeachGame(storedGame.getId(), storedGame.getCreatedBy(), storedGame.getRefereeName(),
                                                      storedGame.getCreatedAt(), storedGame.getScheduledAt(), storedGame.getRules());
                beachGame.getLeague().setAll(storedGame.getLeague());
                beachGame.restoreTeams(storedGame);
                game = beachGame;
            }
            case INDOOR_4X4 -> {
                Indoor4x4Game indoor4x4Game = createIndoor4x4Game(storedGame.getId(), storedGame.getCreatedBy(),
                                                                  storedGame.getRefereeName(), storedGame.getCreatedAt(),
                                                                  storedGame.getScheduledAt(), storedGame.getRules());
                indoor4x4Game.setUsage(storedGame.getUsage());
                indoor4x4Game.getLeague().setAll(storedGame.getLeague());
                indoor4x4Game.restoreTeams(storedGame);
                game = indoor4x4Game;
            }
            case SNOW -> {
                SnowGame snowGame = createSnowGame(storedGame.getId(), storedGame.getCreatedBy(), storedGame.getRefereeName(),
                                                   storedGame.getCreatedAt(), storedGame.getScheduledAt(), storedGame.getRules());
                snowGame.getLeague().setAll(storedGame.getLeague());
                snowGame.restoreTeams(storedGame);
                game = snowGame;
            }
        }

        return game;
    }
}
