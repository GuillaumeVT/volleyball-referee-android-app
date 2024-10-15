package com.tonkar.volleyballreferee.engine.game;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class BeachGameTest {

    @Test
    public void winSet_normal() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
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
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
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
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
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
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
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
