package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class GameTest {

    @Test
    public void point_remove() {
        GameService game = GameFactory.createIndoorGame("VBR");
        game.initTeams();

        assertEquals(0, game.getPoints(TeamType.HOME));
        game.removeLastPoint();
        assertEquals(0, game.getPoints(TeamType.HOME));
    }

    @Test
    public void service_swapFirst() {
        GameService game = GameFactory.createIndoorGame("VBR");
        game.initTeams();

        assertEquals(TeamType.HOME, game.getServingTeam());
        game.swapServiceAtStart();
        assertEquals(TeamType.GUEST, game.getServingTeam());
    }

    @Test
    public void service_keep() {
        GameService game = GameFactory.createIndoorGame("VBR");
        game.initTeams();

        assertEquals(TeamType.HOME, game.getServingTeam());
        game.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, game.getServingTeam());
    }

    @Test
    public void service_sideOut() {
        GameService game = GameFactory.createIndoorGame("VBR");
        game.initTeams();

        assertEquals(TeamType.HOME, game.getServingTeam());
        game.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, game.getServingTeam());
    }

    @Test
    public void service_keep_Reverse() {
        GameService game = GameFactory.createIndoorGame("VBR");
        game.initTeams();

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, game.getServingTeam());
        game.removeLastPoint();
        assertEquals(TeamType.HOME, game.getServingTeam());
    }

    @Test
    public void service_sideOut_Reverse() {
        GameService game = GameFactory.createIndoorGame("VBR");
        game.initTeams();

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, game.getServingTeam());
        game.removeLastPoint();
        assertEquals(TeamType.HOME, game.getServingTeam());
    }

    @Test
    public void timeout() {
        GameService game = GameFactory.createIndoorGame("VBR");
        game.initTeams();

        assertEquals(game.getRules().getTeamTimeoutsPerSet(), game.getRemainingTimeouts(TeamType.HOME));
        game.callTimeout(TeamType.HOME);
        assertEquals(game.getRules().getTeamTimeoutsPerSet() - 1, game.getRemainingTimeouts(TeamType.HOME));
        assertEquals(1, game.getCalledTimeouts(TeamType.HOME).size());
    }

}
