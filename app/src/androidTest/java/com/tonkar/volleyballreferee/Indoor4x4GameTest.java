package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.Indoor4x4Game;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class Indoor4x4GameTest {

    @Test
    public void winSet_normal() {
        GameService game = GameFactory.createIndoor4x4Game("VBR");
        game.initTeams();

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
        GameService game = GameFactory.createIndoor4x4Game("VBR");
        game.initTeams();

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
        GameService game = GameFactory.createIndoor4x4Game("VBR");
        game.initTeams();

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

        assertEquals(true, game.isMatchCompleted());
        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));
        assertEquals(4, game.getNumberOfSets());
    }

    @Test
    public void winGame_tieBreak() {
        GameService game = GameFactory.createIndoor4x4Game("VBR");
        game.initTeams();

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

        assertEquals(true, game.isMatchCompleted());
        assertEquals(2, game.getSets(TeamType.HOME));
        assertEquals(3, game.getSets(TeamType.GUEST));
        assertEquals(5, game.getNumberOfSets());
    }

    @Test
    public void substitutePlayer_Pos1Server() {
        Indoor4x4Game game = GameFactory.createIndoor4x4Game("VBR");

        for (int index = 1; index <= 8; index++) {
            game.addPlayer(TeamType.HOME, index);
            game.addPlayer(TeamType.GUEST, index);
        }

        game.initTeams();

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
        assertEquals(true, game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
        assertEquals(false, game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_2).contains(1));

        game.addPoint(TeamType.HOME);
        assertEquals(2, game.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1));
        assertEquals(false, game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
        assertEquals(true, game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_2).contains(1));

        game.addPoint(TeamType.GUEST);
        assertEquals(true, game.getPossibleSubstitutions(TeamType.HOME, PositionType.POSITION_1).contains(1));
    }
}
