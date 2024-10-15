package com.tonkar.volleyballreferee.engine.game;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class GameTest {

    @Test
    public void point_remove() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        game.startMatch();

        assertEquals(0, game.getPoints(TeamType.HOME));
        game.undoGameEvent(GameEvent.newPointEvent(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.HOME));
    }

    @Test
    public void service_swapFirst() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        game.startMatch();

        assertEquals(TeamType.HOME, game.getServingTeam());
        game.swapServiceAtStart();
        assertEquals(TeamType.GUEST, game.getServingTeam());
    }

    @Test
    public void service_keep() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        game.startMatch();

        assertEquals(TeamType.HOME, game.getServingTeam());
        game.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, game.getServingTeam());
    }

    @Test
    public void service_sideOut() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        game.startMatch();

        assertEquals(TeamType.HOME, game.getServingTeam());
        game.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, game.getServingTeam());
    }

    @Test
    public void service_keep_Reverse() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        game.startMatch();

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, game.getServingTeam());
        game.undoGameEvent(GameEvent.newPointEvent(TeamType.HOME));
        assertEquals(TeamType.HOME, game.getServingTeam());
    }

    @Test
    public void service_sideOut_Reverse() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        game.startMatch();

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, game.getServingTeam());
        game.undoGameEvent(GameEvent.newPointEvent(TeamType.GUEST));
        assertEquals(TeamType.HOME, game.getServingTeam());
    }

    @Test
    public void timeout() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        game.startMatch();

        assertEquals(game.getRules().getTeamTimeoutsPerSet(), game.countRemainingTimeouts(TeamType.HOME));
        game.callTimeout(TeamType.HOME);
        assertEquals(game.getRules().getTeamTimeoutsPerSet() - 1, game.countRemainingTimeouts(TeamType.HOME));
        assertEquals(1, game.getCalledTimeouts(TeamType.HOME).size());
    }

}
