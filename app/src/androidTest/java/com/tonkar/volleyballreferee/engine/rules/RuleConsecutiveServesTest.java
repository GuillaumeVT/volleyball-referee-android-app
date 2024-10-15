package com.tonkar.volleyballreferee.engine.rules;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class RuleConsecutiveServesTest {

    @Test
    public void consecutive_unlimited() {
        IndoorGame game = createGame(9999);

        for (int index = 0; index < 15; index++) {
            game.addPoint(TeamType.HOME);
            assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
            assertFalse(game.samePlayerHadServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
            assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
            assertFalse(game.samePlayerHadServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        }
    }

    @Test
    public void consecutive_3() {
        IndoorGame game = createGame(3);

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertTrue(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.removeLastPoint();
        assertTrue(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.removeLastPoint();
        assertTrue(game.samePlayerHadServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
    }

    @Test
    public void consecutive_3_opposite() {
        IndoorGame game = createGame(3);

        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.GUEST);
        assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.GUEST);
        assertTrue(game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.removeLastPoint();
        assertTrue(game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.removeLastPoint();
        assertTrue(game.samePlayerHadServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
    }

    @Test
    public void consecutive_2() {
        IndoorGame game = createGame(2);

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertTrue(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.GUEST);
        assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        assertTrue(game.samePlayerHadServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.GUEST);
        assertTrue(game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertFalse(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertTrue(game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
    }

    private IndoorGame createGame(int consecutiveServes) {
        Rules rules = new Rules(UUID.randomUUID().toString(), null, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                System.currentTimeMillis(), "My rules", GameType.INDOOR, 5, 25, true, 15, true, true, Rules.WIN_TERMINATION,
                                true, 2, 30, true, 60, true, 180, Rules.FIVB_LIMITATION, 6, false, 0, 0, consecutiveServes);

        IndoorGame game = new IndoorGame(UUID.randomUUID().toString(), null, "",
                                         Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(),
                                         rules);

        for (int index = 1; index <= 6; index++) {
            game.addPlayer(TeamType.HOME, index);
            game.addPlayer(TeamType.GUEST, index);
        }

        game.startMatch();

        for (int index = 1; index <= 6; index++) {
            game.substitutePlayer(TeamType.HOME, index, PositionType.fromInt(index), ActionOriginType.USER);
            game.substitutePlayer(TeamType.GUEST, index, PositionType.fromInt(index), ActionOriginType.USER);
        }

        game.confirmStartingLineup(TeamType.HOME);
        game.confirmStartingLineup(TeamType.GUEST);

        return game;
    }
}
