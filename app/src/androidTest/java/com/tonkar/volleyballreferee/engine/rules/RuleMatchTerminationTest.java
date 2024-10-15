package com.tonkar.volleyballreferee.engine.rules;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class RuleMatchTerminationTest {

    @Test
    public void matchTermination_allSets_1() {
        Rules rules = new Rules(UUID.randomUUID().toString(), null, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                System.currentTimeMillis(), "My rules", GameType.INDOOR, 1, 25, true, 15, true, true,
                                Rules.ALL_SETS_TERMINATION, true, 2, 30, true, 60, true, 180, Rules.FIVB_LIMITATION, 6, false, 0, 0, 9999);

        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), rules);
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        assertTrue(game.isMatchCompleted());
        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(1, game.getNumberOfSets());
    }

    @Test
    public void matchTermination_allSets_3() {
        Rules rules = new Rules(UUID.randomUUID().toString(), null, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                System.currentTimeMillis(), "My rules", GameType.INDOOR, 3, 25, true, 15, true, true,
                                Rules.ALL_SETS_TERMINATION, true, 2, 30, true, 60, true, 180, Rules.FIVB_LIMITATION, 6, false, 0, 0, 9999);

        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), rules);
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.GUEST);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.GUEST);
        }

        assertFalse(game.isMatchCompleted());
        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(2, game.getSets(TeamType.GUEST));

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        assertTrue(game.isMatchCompleted());
        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(2, game.getSets(TeamType.GUEST));
        assertEquals(3, game.getNumberOfSets());
    }

    @Test
    public void matchTermination_allSets_5() {
        Rules rules = new Rules(UUID.randomUUID().toString(), null, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                System.currentTimeMillis(), "My rules", GameType.INDOOR, 5, 25, true, 15, true, true,
                                Rules.ALL_SETS_TERMINATION, true, 2, 30, true, 60, true, 180, Rules.FIVB_LIMITATION, 6, false, 0, 0, 9999);

        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), rules);
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        assertFalse(game.isMatchCompleted());
        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.GUEST);
        }

        assertFalse(game.isMatchCompleted());
        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        assertTrue(game.isMatchCompleted());
        assertEquals(4, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));
        assertEquals(5, game.getNumberOfSets());
    }
}
