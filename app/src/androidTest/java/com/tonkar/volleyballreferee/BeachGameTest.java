package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BeachGameTest {

    @Test
    public void winSet_normal() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            assertEquals(index, game.getPoints(TeamType.HOME));
            game.addPoint(TeamType.HOME);
        }

        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(2, game.getNumberOfSets());
    }

    @Test
    public void winSet_2PointsGap() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            assertEquals(index, game.getPoints(TeamType.HOME));
            game.addPoint(TeamType.HOME);
            assertEquals(index, game.getPoints(TeamType.GUEST));
            game.addPoint(TeamType.GUEST);
        }

        assertEquals(21, game.getPoints(TeamType.HOME));
        assertEquals(21, game.getPoints(TeamType.GUEST));

        game.addPoint(TeamType.HOME);
        assertEquals(22, game.getPoints(TeamType.HOME));
        game.addPoint(TeamType.HOME);
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));
        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(2, game.getNumberOfSets());
    }

    @Test
    public void winGame_normal() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        assertTrue(game.isMatchCompleted());
        assertEquals(2, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(2, game.getNumberOfSets());
    }

    @Test
    public void winGame_tieBreak() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.GUEST);
        }

        for (int index = 0; index < 15; index++) {
            game.addPoint(TeamType.GUEST);
        }

        assertTrue(game.isMatchCompleted());
        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(2, game.getSets(TeamType.GUEST));
        assertEquals(3, game.getNumberOfSets());
    }

}
