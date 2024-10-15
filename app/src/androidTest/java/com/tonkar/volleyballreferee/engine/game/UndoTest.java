package com.tonkar.volleyballreferee.engine.game;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class UndoTest {

    @Test
    public void undoLastPoint() {
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                          System.currentTimeMillis(), Rules.officialBeachRules());
        beachGame.startMatch();

        beachGame.addPoint(TeamType.HOME);
        List<GameEvent> gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.POINT, gameEvents.get(0).getEventType());

        beachGame.addPoint(TeamType.GUEST);
        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.POINT, gameEvents.get(0).getEventType());

        beachGame.undoGameEvent(gameEvents.get(0));

        beachGame.addPoint(TeamType.HOME);
        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.POINT, gameEvents.get(0).getEventType());
    }

    @Test
    public void undoTimeout() {
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                          System.currentTimeMillis(), Rules.officialBeachRules());
        beachGame.startMatch();

        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.callTimeout(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.callTimeout(TeamType.GUEST);

        List<GameEvent> gameEvents = beachGame.getLatestGameEvents();
        assertEquals(2, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(1).getTeamType());
        assertEquals(GameEvent.EventType.TIMEOUT, gameEvents.get(1).getEventType());

        beachGame.undoGameEvent(gameEvents.get(1));

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.POINT, gameEvents.get(0).getEventType());

        beachGame.undoGameEvent(gameEvents.get(0));

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(2, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(1).getTeamType());
        assertEquals(GameEvent.EventType.TIMEOUT, gameEvents.get(1).getEventType());

        beachGame.undoGameEvent(gameEvents.get(1));

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.POINT, gameEvents.get(0).getEventType());
    }

    @Test
    public void undoSubstitution() {
        IndoorGame indoorGame = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                             System.currentTimeMillis(), Rules.officialIndoorRules());
        fillTeam(indoorGame, TeamType.HOME);
        fillTeam(indoorGame, TeamType.GUEST);
        indoorGame.startMatch();

        fillCourts(indoorGame, 6);

        indoorGame.substitutePlayer(TeamType.HOME, 7, PositionType.POSITION_1, ActionOriginType.USER);
        assertEquals(7, indoorGame.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        List<GameEvent> gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoorGame.substitutePlayer(TeamType.GUEST, 7, PositionType.POSITION_2, ActionOriginType.USER);
        assertEquals(7, indoorGame.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_2));

        gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(2, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(1).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(1).getEventType());

        indoorGame.undoGameEvent(gameEvents.get(0));
        assertEquals(1, indoorGame.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoorGame.undoGameEvent(gameEvents.get(0));
        assertEquals(2, indoorGame.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));

        gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(0, gameEvents.size());

        // Indoor 4x4

        Indoor4x4Game indoor4x4Game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                                      System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());
        fillTeam(indoor4x4Game, TeamType.HOME);
        fillTeam(indoor4x4Game, TeamType.GUEST);
        indoor4x4Game.startMatch();

        fillCourts(indoor4x4Game, 4);

        indoor4x4Game.substitutePlayer(TeamType.HOME, 7, PositionType.POSITION_1, ActionOriginType.USER);
        assertEquals(7, indoor4x4Game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoor4x4Game.substitutePlayer(TeamType.GUEST, 7, PositionType.POSITION_2, ActionOriginType.USER);
        assertEquals(7, indoor4x4Game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_2));

        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(2, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(1).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(1).getEventType());

        indoor4x4Game.undoGameEvent(gameEvents.get(0));
        assertEquals(1, indoor4x4Game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoor4x4Game.undoGameEvent(gameEvents.get(0));
        assertEquals(2, indoor4x4Game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));

        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(0, gameEvents.size());
    }

    @Test
    public void undoSanction() {
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                          System.currentTimeMillis(), Rules.officialBeachRules());
        beachGame.startMatch();

        beachGame.giveSanction(TeamType.HOME, SanctionType.DELAY_WARNING, 1);
        beachGame.giveSanction(TeamType.HOME, SanctionType.DELAY_PENALTY, 1);

        List<GameEvent> gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.POINT, gameEvents.get(0).getEventType());

        beachGame.undoGameEvent(gameEvents.get(0));

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(2, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SANCTION, gameEvents.get(0).getEventType());
        assertEquals(SanctionType.DELAY_WARNING, gameEvents.get(0).getSanction().getCard());
        assertEquals(TeamType.HOME, gameEvents.get(1).getTeamType());
        assertEquals(GameEvent.EventType.SANCTION, gameEvents.get(1).getEventType());
        assertEquals(SanctionType.DELAY_PENALTY, gameEvents.get(1).getSanction().getCard());

        beachGame.undoGameEvent(gameEvents.get(1));
        beachGame.undoGameEvent(gameEvents.get(0));

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(0, gameEvents.size());

        beachGame.giveSanction(TeamType.GUEST, SanctionType.RED, 2);

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.POINT, gameEvents.get(0).getEventType());

        beachGame.undoGameEvent(gameEvents.get(0));

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SANCTION, gameEvents.get(0).getEventType());
        assertEquals(SanctionType.RED, gameEvents.get(0).getSanction().getCard());

        beachGame.undoGameEvent(gameEvents.get(0));

        gameEvents = beachGame.getLatestGameEvents();
        assertEquals(0, gameEvents.size());
    }

    @Test
    public void undoSanction_Expulsion() {
        // Indoor

        IndoorGame indoorGame = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                             System.currentTimeMillis(), Rules.officialIndoorRules());
        fillTeam(indoorGame, TeamType.HOME);
        fillTeam(indoorGame, TeamType.GUEST);
        indoorGame.startMatch();

        fillCourts(indoorGame, 6);

        indoorGame.giveSanction(TeamType.HOME, SanctionType.RED_EXPULSION, 4);
        indoorGame.substitutePlayer(TeamType.HOME, 7, PositionType.POSITION_4, ActionOriginType.USER);
        assertEquals(7, indoorGame.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_4));

        List<GameEvent> gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SANCTION, gameEvents.get(0).getEventType());
        assertEquals(SanctionType.RED_EXPULSION, gameEvents.get(0).getSanction().getCard());

        indoorGame.undoGameEvent(gameEvents.get(0));

        gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoorGame.undoGameEvent(gameEvents.get(0));

        assertEquals(4, indoorGame.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_4));
        gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(0, gameEvents.size());

        // Indoor 4x4

        Indoor4x4Game indoor4x4Game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                                      System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());
        fillTeam(indoor4x4Game, TeamType.HOME);
        fillTeam(indoor4x4Game, TeamType.GUEST);
        indoor4x4Game.startMatch();

        fillCourts(indoor4x4Game, 4);

        indoor4x4Game.giveSanction(TeamType.HOME, SanctionType.RED_EXPULSION, 2);
        indoor4x4Game.substitutePlayer(TeamType.HOME, 6, PositionType.POSITION_2, ActionOriginType.USER);
        assertEquals(6, indoor4x4Game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));

        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SANCTION, gameEvents.get(0).getEventType());
        assertEquals(SanctionType.RED_EXPULSION, gameEvents.get(0).getSanction().getCard());

        indoor4x4Game.undoGameEvent(gameEvents.get(0));

        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoor4x4Game.undoGameEvent(gameEvents.get(0));

        assertEquals(2, indoor4x4Game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));
        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(0, gameEvents.size());
    }

    @Test
    public void undoSanction_Disqualification() {
        // Indoor

        IndoorGame indoorGame = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                             System.currentTimeMillis(), Rules.officialIndoorRules());
        fillTeam(indoorGame, TeamType.HOME);
        fillTeam(indoorGame, TeamType.GUEST);
        indoorGame.startMatch();

        fillCourts(indoorGame, 6);

        indoorGame.giveSanction(TeamType.GUEST, SanctionType.RED_DISQUALIFICATION, 5);
        indoorGame.substitutePlayer(TeamType.GUEST, 7, PositionType.POSITION_5, ActionOriginType.USER);
        assertEquals(7, indoorGame.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_5));

        List<GameEvent> gameEvents = indoorGame.getLatestGameEvents();

        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SANCTION, gameEvents.get(0).getEventType());
        assertEquals(SanctionType.RED_DISQUALIFICATION, gameEvents.get(0).getSanction().getCard());

        indoorGame.undoGameEvent(gameEvents.get(0));

        gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.GUEST, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoorGame.undoGameEvent(gameEvents.get(0));

        assertEquals(5, indoorGame.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_5));
        gameEvents = indoorGame.getLatestGameEvents();
        assertEquals(0, gameEvents.size());

        // Indoor 4x4

        Indoor4x4Game indoor4x4Game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                                      System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());
        fillTeam(indoor4x4Game, TeamType.HOME);
        fillTeam(indoor4x4Game, TeamType.GUEST);
        indoor4x4Game.startMatch();

        fillCourts(indoor4x4Game, 4);

        indoor4x4Game.giveSanction(TeamType.HOME, SanctionType.RED_DISQUALIFICATION, 3);
        indoor4x4Game.substitutePlayer(TeamType.HOME, 7, PositionType.POSITION_3, ActionOriginType.USER);
        assertEquals(7, indoor4x4Game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_3));

        gameEvents = indoor4x4Game.getLatestGameEvents();

        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SANCTION, gameEvents.get(0).getEventType());
        assertEquals(SanctionType.RED_DISQUALIFICATION, gameEvents.get(0).getSanction().getCard());

        indoor4x4Game.undoGameEvent(gameEvents.get(0));

        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(1, gameEvents.size());
        assertEquals(TeamType.HOME, gameEvents.get(0).getTeamType());
        assertEquals(GameEvent.EventType.SUBSTITUTION, gameEvents.get(0).getEventType());

        indoor4x4Game.undoGameEvent(gameEvents.get(0));

        assertEquals(3, indoor4x4Game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_3));
        gameEvents = indoor4x4Game.getLatestGameEvents();
        assertEquals(0, gameEvents.size());
    }

    private void fillTeam(IGame game, TeamType teamType) {
        for (int number = 1; number <= 8; number++) {
            game.addPlayer(teamType, number);
        }
        game.addLibero(teamType, 8);
    }

    private void fillCourts(IClassicTeam indoorTeam, int positions) {
        for (int number = 1; number <= positions; number++) {
            indoorTeam.substitutePlayer(TeamType.HOME, number, PositionType.fromInt(number), ActionOriginType.USER);
            indoorTeam.substitutePlayer(TeamType.GUEST, number, PositionType.fromInt(number), ActionOriginType.USER);
        }
        indoorTeam.confirmStartingLineup(TeamType.HOME);
        indoorTeam.confirmStartingLineup(TeamType.GUEST);
    }
}
