package com.tonkar.volleyballreferee.engine.game;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class SnowGameTest {

    @Test
    public void service() {
        SnowGame game = GameFactory.createSnowGame(UUID.randomUUID().toString(), null, "",
                                                   Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                   System.currentTimeMillis(), Rules.officialSnowRules());
        game.startMatch();

        game.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_1, ActionOriginType.USER);
        game.substitutePlayer(TeamType.HOME, 2, PositionType.POSITION_2, ActionOriginType.USER);
        game.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_3, ActionOriginType.USER);
        game.confirmStartingLineup(TeamType.HOME);

        game.substitutePlayer(TeamType.GUEST, 1, PositionType.POSITION_1, ActionOriginType.USER);
        game.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_2, ActionOriginType.USER);
        game.substitutePlayer(TeamType.GUEST, 3, PositionType.POSITION_3, ActionOriginType.USER);
        game.confirmStartingLineup(TeamType.GUEST);

        game.swapServiceAtStart();

        game.addPoint(TeamType.GUEST);
        assertEquals(1, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_1));
        assertEquals(2, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_2));
        assertEquals(3, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_3));

        game.addPoint(TeamType.HOME);
        assertEquals(1, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));
        assertEquals(2, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));
        assertEquals(3, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_3));

        game.addPoint(TeamType.GUEST);
        assertEquals(1, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_3));
        assertEquals(2, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_1));
        assertEquals(3, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_2));

        game.addPoint(TeamType.HOME);
        assertEquals(1, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_3));
        assertEquals(2, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));
        assertEquals(3, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));

        game.addPoint(TeamType.GUEST);
        assertEquals(1, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_2));
        assertEquals(2, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_3));
        assertEquals(3, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_1));

        game.addPoint(TeamType.HOME);
        assertEquals(1, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));
        assertEquals(2, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_3));
        assertEquals(3, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        game.addPoint(TeamType.GUEST);
        assertEquals(1, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_1));
        assertEquals(2, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_2));
        assertEquals(3, game.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_3));
    }

    @Test
    public void substitution() {
        SnowGame game = GameFactory.createSnowGame(UUID.randomUUID().toString(), null, "",
                                                   Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                   System.currentTimeMillis(), Rules.officialSnowRules());
        game.addPlayer(TeamType.HOME, 4);
        game.startMatch();

        game.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_1, ActionOriginType.USER);
        game.substitutePlayer(TeamType.HOME, 2, PositionType.POSITION_2, ActionOriginType.USER);
        game.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_3, ActionOriginType.USER);
        game.confirmStartingLineup(TeamType.HOME);

        game.substitutePlayer(TeamType.GUEST, 1, PositionType.POSITION_1, ActionOriginType.USER);
        game.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_2, ActionOriginType.USER);
        game.substitutePlayer(TeamType.GUEST, 3, PositionType.POSITION_3, ActionOriginType.USER);
        game.confirmStartingLineup(TeamType.GUEST);

        game.substitutePlayer(TeamType.HOME, 4, PositionType.POSITION_1, ActionOriginType.USER);
        assertEquals(4, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));
        assertEquals(PositionType.POSITION_1, game.getPlayerPosition(TeamType.HOME, 4));
        assertEquals(PositionType.BENCH, game.getPlayerPosition(TeamType.HOME, 1));
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_2).contains(1));
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_3).contains(1));

        game.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_2, ActionOriginType.USER);
        assertEquals(1, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2));
        assertEquals(PositionType.POSITION_2, game.getPlayerPosition(TeamType.HOME, 1));
        assertEquals(PositionType.BENCH, game.getPlayerPosition(TeamType.HOME, 2));

        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).isEmpty());
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_2).isEmpty());
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_3).isEmpty());

        assertTrue(game.getPossibleSubstitutions(TeamType.GUEST, PositionType.POSITION_1).isEmpty());
        assertTrue(game.getPossibleSubstitutions(TeamType.GUEST, PositionType.POSITION_2).isEmpty());
        assertTrue(game.getPossibleSubstitutions(TeamType.GUEST, PositionType.POSITION_3).isEmpty());
    }

}
