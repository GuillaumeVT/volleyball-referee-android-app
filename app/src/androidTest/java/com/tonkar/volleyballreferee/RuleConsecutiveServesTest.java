package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.game.IndoorGame;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RuleConsecutiveServesTest {

    @Test
    public void consecutive_unlimited() {
        IndoorGame game = createGame(9999);

        for (int index = 0; index < 15; index++) {
            game.addPoint(TeamType.HOME);
            assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
            assertEquals(false, game.samePlayerHadServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
            assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
            assertEquals(false, game.samePlayerHadServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        }
    }

    @Test
    public void consecutive_3() {
        IndoorGame game = createGame(3);

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertEquals(true, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.removeLastPoint();
        assertEquals(true, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.removeLastPoint();
        assertEquals(true, game.samePlayerHadServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
    }

    @Test
    public void consecutive_3_opposite() {
        IndoorGame game = createGame(3);

        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.GUEST);
        assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.GUEST);
        assertEquals(true, game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.removeLastPoint();
        assertEquals(true, game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.removeLastPoint();
        assertEquals(true, game.samePlayerHadServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
    }

    @Test
    public void consecutive_2() {
        IndoorGame game = createGame(2);

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertEquals(true, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.GUEST);
        assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        assertEquals(true, game.samePlayerHadServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.GUEST);
        assertEquals(true, game.samePlayerServedNConsecutiveTimes(TeamType.GUEST, game.getPoints(TeamType.GUEST), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.HOME);
        assertEquals(false, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
        game.addPoint(TeamType.HOME);
        assertEquals(true, game.samePlayerServedNConsecutiveTimes(TeamType.HOME, game.getPoints(TeamType.HOME), game.getPointsLadder()));
    }

    private IndoorGame createGame(int consecutiveServes) {
        IndoorGame game = new IndoorGame(new Rules(5, 25, true, true, true, true, 2, 30,
                true, 60, true, 180, 6, false, consecutiveServes), "VBR");

        for (int index = 1; index <= 6; index++) {
            game.addPlayer(TeamType.HOME, index);
            game.addPlayer(TeamType.GUEST, index);
        }

        game.initTeams();

        for (int index = 1; index <= 6; index++) {
            game.substitutePlayer(TeamType.HOME, index, PositionType.fromInt(index), ActionOriginType.USER);
            game.substitutePlayer(TeamType.GUEST, index, PositionType.fromInt(index), ActionOriginType.USER);
        }

        game.confirmStartingLineup();

        return game;
    }
}
