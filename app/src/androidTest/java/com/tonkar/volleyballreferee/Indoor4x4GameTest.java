package com.tonkar.volleyballreferee;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.Indoor4x4Game;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.business.rules.Rules;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class Indoor4x4GameTest {

    @Test
    public void winSet_normal() {
        GameService game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), Authentication.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());
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
        GameService game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), Authentication.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            assertEquals(index, game.getPoints(TeamType.HOME));
            game.addPoint(TeamType.HOME);
            assertEquals(index, game.getPoints(TeamType.GUEST));
            game.addPoint(TeamType.GUEST);
        }

        assertEquals(25, game.getPoints(TeamType.HOME));
        assertEquals(25, game.getPoints(TeamType.GUEST));

        game.addPoint(TeamType.HOME);
        assertEquals(26, game.getPoints(TeamType.HOME));
        game.addPoint(TeamType.HOME);
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));
        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(2, game.getNumberOfSets());
    }

    @Test
    public void winGame_normal() {
        GameService game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), Authentication.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.GUEST);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        assertTrue(game.isMatchCompleted());
        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));
        assertEquals(4, game.getNumberOfSets());
    }

    @Test
    public void winGame_tieBreak() {
        GameService game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), Authentication.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());
        game.startMatch();

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.HOME);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.GUEST);
        }

        for (int index = 0; index < game.getRules().getPointsPerSet(); index++) {
            game.addPoint(TeamType.GUEST);
        }

        for (int index = 0; index < 15; index++) {
            game.addPoint(TeamType.GUEST);
        }

        assertTrue(game.isMatchCompleted());
        assertEquals(2, game.getSets(TeamType.HOME));
        assertEquals(3, game.getSets(TeamType.GUEST));
        assertEquals(5, game.getNumberOfSets());
    }

    @Test
    public void substitutePlayer_Pos1Server_defense() {
        Indoor4x4Game game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), Authentication.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());

        for (int index = 1; index <= 8; index++) {
            game.addPlayer(TeamType.HOME, index);
            game.addPlayer(TeamType.GUEST, index);
        }

        game.startMatch();

        for (int index = 1; index <= 4; index++) {
            game.substitutePlayer(TeamType.HOME, index, PositionType.fromInt(index), ActionOriginType.USER);
            game.substitutePlayer(TeamType.GUEST, index, PositionType.fromInt(index), ActionOriginType.USER);
        }

        game.confirmStartingLineup();

        game.addPoint(TeamType.HOME);
        assertEquals(1, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        game.addPoint(TeamType.GUEST);
        assertEquals(1, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        game.substitutePlayer(TeamType.HOME, 5, PositionType.POSITION_1, ActionOriginType.USER);
        assertEquals(5, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
        assertFalse(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_2).contains(1));

        game.addPoint(TeamType.HOME);
        assertEquals(2, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));
        assertFalse(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_2).contains(1));

        game.addPoint(TeamType.GUEST);
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
    }

    @Test
    public void substitutePlayer_Pos1Server_attack() {
        Indoor4x4Game game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), Authentication.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());

        for (int index = 1; index <= 8; index++) {
            game.addPlayer(TeamType.HOME, index);
            game.addPlayer(TeamType.GUEST, index);
        }

        game.startMatch();

        for (int index = 1; index <= 4; index++) {
            game.substitutePlayer(TeamType.HOME, index, PositionType.fromInt(index), ActionOriginType.USER);
            game.substitutePlayer(TeamType.GUEST, index, PositionType.fromInt(index), ActionOriginType.USER);
        }

        game.confirmStartingLineup();

        game.addPoint(TeamType.HOME);
        assertEquals(1, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));

        game.substitutePlayer(TeamType.HOME, 5, PositionType.POSITION_1, ActionOriginType.USER);
        assertEquals(5, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));
        assertTrue(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
        assertFalse(game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_2).contains(1));
    }

}
