package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
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

    public static GameService createGame(RecordedGameService recordedGameService) {
        Log.i("VBR-Core", "Create web game");
        GameService gameService = null;

        switch (recordedGameService.getGameType()) {
            case INDOOR:
                IndoorGame indoorGame = createIndoorGame(recordedGameService.getGameDate(), recordedGameService.getGameSchedule(), recordedGameService.getRules());
                indoorGame.setUsageType(recordedGameService.getUsageType());
                indoorGame.setLeagueName(recordedGameService.getLeagueName());
                fillIndoorTeamsFrom(recordedGameService, indoorGame);
                gameService = indoorGame;
                break;
            case BEACH:
                BeachGame beachGame = createBeachGame(recordedGameService.getGameDate(), recordedGameService.getGameSchedule(), recordedGameService.getRules());
                beachGame.setLeagueName(recordedGameService.getLeagueName());
                fillTeamsFrom(recordedGameService, beachGame);
                gameService = beachGame;
                break;
            case INDOOR_4X4:
                Indoor4x4Game indoor4x4Game = createIndoor4x4Game(recordedGameService.getGameDate(), recordedGameService.getGameSchedule(), recordedGameService.getRules());
                indoor4x4Game.setUsageType(recordedGameService.getUsageType());
                indoor4x4Game.setLeagueName(recordedGameService.getLeagueName());
                fillIndoorTeamsFrom(recordedGameService, indoor4x4Game);
                gameService = indoor4x4Game;
                break;
        }

        return gameService;
    }

    private static void fillTeamsFrom(RecordedGameService recordedGameService, BaseTeamService teamService) {
        teamService.setTeamName(TeamType.HOME, recordedGameService.getTeamName(TeamType.HOME));
        teamService.setTeamColor(TeamType.HOME, recordedGameService.getTeamColor(TeamType.HOME));
        teamService.setGenderType(TeamType.HOME, recordedGameService.getGenderType(TeamType.HOME));

        for (int number : recordedGameService.getPlayers(TeamType.HOME))  {
            teamService.addPlayer(TeamType.HOME, number);
        }

        teamService.setTeamName(TeamType.GUEST, recordedGameService.getTeamName(TeamType.GUEST));
        teamService.setTeamColor(TeamType.GUEST, recordedGameService.getTeamColor(TeamType.GUEST));
        teamService.setGenderType(TeamType.GUEST, recordedGameService.getGenderType(TeamType.GUEST));

        for (int number : recordedGameService.getPlayers(TeamType.GUEST))  {
            teamService.addPlayer(TeamType.GUEST, number);
        }
    }

    private static void fillIndoorTeamsFrom(RecordedGameService recordedGameService, BaseIndoorTeamService indoorTeamService) {
        fillTeamsFrom(recordedGameService, indoorTeamService);

        indoorTeamService.setCaptain(TeamType.HOME, recordedGameService.getCaptain(TeamType.HOME));
        indoorTeamService.setLiberoColor(TeamType.HOME, recordedGameService.getLiberoColor(TeamType.HOME));

        for (int number : recordedGameService.getLiberos(TeamType.HOME))  {
            indoorTeamService.addPlayer(TeamType.HOME, number);
            indoorTeamService.addLibero(TeamType.HOME, number);
        }

        indoorTeamService.setCaptain(TeamType.GUEST, recordedGameService.getCaptain(TeamType.GUEST));
        indoorTeamService.setLiberoColor(TeamType.GUEST, recordedGameService.getLiberoColor(TeamType.GUEST));

        for (int number : recordedGameService.getLiberos(TeamType.GUEST))  {
            indoorTeamService.addPlayer(TeamType.GUEST, number);
            indoorTeamService.addLibero(TeamType.GUEST, number);
        }
    }
}
