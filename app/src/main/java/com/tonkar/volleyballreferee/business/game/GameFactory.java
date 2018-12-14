package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.rules.Rules;

public class GameFactory {

    public static IndoorGame createIndoorGame(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i(Tags.FACTORY, "Create indoor game rules");
        return new IndoorGame(gameDate, gameSchedule, rules);
    }

    public static BeachGame createBeachGame(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i(Tags.FACTORY, "Create beach game");
        return new BeachGame(gameDate, gameSchedule, rules);
    }

    public static IndoorGame createPointBasedGame(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i(Tags.FACTORY, "Create score-based game");
        IndoorGame game = createIndoorGame(gameDate, gameSchedule, rules);
        game.setUsageType(UsageType.POINTS_SCOREBOARD);
        return game;
    }

    public static TimeBasedGame createTimeBasedGame(final long gameDate, final long gameSchedule) {
        Log.i(Tags.FACTORY, "Create time-based game");
        return new TimeBasedGame(gameDate, gameSchedule);
    }

    public static Indoor4x4Game createIndoor4x4Game(final long gameDate, final long gameSchedule, final Rules rules) {
        Log.i(Tags.FACTORY, "Create indoor 4x4 game");
        return new Indoor4x4Game(gameDate, gameSchedule, rules);
    }

    public static GameService createGame(RecordedGameService recordedGameService) {
        Log.i(Tags.FACTORY, "Create game from web");
        GameService gameService = null;

        switch (recordedGameService.getGameType()) {
            case INDOOR:
                IndoorGame indoorGame = createIndoorGame(recordedGameService.getGameDate(), recordedGameService.getGameSchedule(), recordedGameService.getRules());
                indoorGame.setUsageType(recordedGameService.getUsageType());
                indoorGame.setIndexed(recordedGameService.isIndexed());
                indoorGame.setLeagueName(recordedGameService.getLeagueName());
                indoorGame.setDivisionName(recordedGameService.getDivisionName());
                indoorGame.restoreTeams(recordedGameService);
                gameService = indoorGame;
                break;
            case BEACH:
                BeachGame beachGame = createBeachGame(recordedGameService.getGameDate(), recordedGameService.getGameSchedule(), recordedGameService.getRules());
                beachGame.setIndexed(recordedGameService.isIndexed());
                beachGame.setLeagueName(recordedGameService.getLeagueName());
                beachGame.setDivisionName(recordedGameService.getDivisionName());
                beachGame.restoreTeams(recordedGameService);
                gameService = beachGame;
                break;
            case INDOOR_4X4:
                Indoor4x4Game indoor4x4Game = createIndoor4x4Game(recordedGameService.getGameDate(), recordedGameService.getGameSchedule(), recordedGameService.getRules());
                indoor4x4Game.setUsageType(recordedGameService.getUsageType());
                indoor4x4Game.setIndexed(recordedGameService.isIndexed());
                indoor4x4Game.setLeagueName(recordedGameService.getLeagueName());
                indoor4x4Game.setDivisionName(recordedGameService.getDivisionName());
                indoor4x4Game.restoreTeams(recordedGameService);
                gameService = indoor4x4Game;
                break;
        }

        return gameService;
    }
}
