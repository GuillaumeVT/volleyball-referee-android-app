package com.tonkar.volleyballreferee.engine.game;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.game.set.IndoorSet;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.IndoorTeamComposition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class IndoorGameTest {

    @Test
    public void winSet_normal() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
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
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
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
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
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
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
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

    private void createTeamWithNPlayers(IndoorGame game, TeamType teamType, int playerCount) {
        for (int index = 1; index <= playerCount; index++) {
            game.addPlayer(teamType, index);
        }

        game.setCaptain(teamType, 1);
        game.addLibero(teamType, playerCount);
    }

    private void initCourt(IndoorGame game, TeamType teamType) {
        for (int index = 1; index <= 6; index++) {
            game.substitutePlayer(teamType, index, PositionType.fromInt(index), ActionOriginType.USER);
        }

        game.confirmStartingLineup(teamType);
    }

    @Test
    public void substitution_libero_singleLine() {
        IndoorGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                       System.currentTimeMillis(), Rules.officialIndoorRules());
        createTeamWithNPlayers(game, TeamType.HOME, 10);
        createTeamWithNPlayers(game, TeamType.GUEST, 10);
        game.startMatch();

        TeamType teamType = TeamType.HOME;
        initCourt(game, teamType);

        IndoorSet set = (IndoorSet) game.currentSet();
        IndoorTeamComposition composition = (IndoorTeamComposition) set.getTeamComposition(teamType);

        int middleBlocker = game.getPlayerAtPosition(teamType, PositionType.POSITION_5);
        int libero = 10;
        game.substitutePlayer(teamType, libero, PositionType.POSITION_5, ActionOriginType.USER);
        int oppositeMiddleBlocker = composition.getPlayerAtPosition(PositionType.POSITION_2);
        assertTrue(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.substitutePlayer(teamType, middleBlocker, PositionType.POSITION_5, ActionOriginType.USER);
        assertFalse(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.substitutePlayer(teamType, libero, PositionType.POSITION_5, ActionOriginType.USER);
        assertTrue(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.substitutePlayer(teamType, middleBlocker, PositionType.POSITION_5, ActionOriginType.APPLICATION);
        assertTrue(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));
    }

    @Test
    public void substitution_libero_singleLine_replace() {
        IndoorGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                       System.currentTimeMillis(), Rules.officialIndoorRules());
        createTeamWithNPlayers(game, TeamType.HOME, 10);
        createTeamWithNPlayers(game, TeamType.GUEST, 10);
        game.startMatch();

        TeamType teamType = TeamType.HOME;
        initCourt(game, teamType);

        IndoorSet set = (IndoorSet) game.currentSet();
        IndoorTeamComposition composition = (IndoorTeamComposition) set.getTeamComposition(teamType);

        int middleBlocker = game.getPlayerAtPosition(teamType, PositionType.POSITION_5);
        int libero = 10;
        game.substitutePlayer(teamType, libero, PositionType.POSITION_5, ActionOriginType.USER);
        int oppositeMiddleBlocker = composition.getPlayerAtPosition(PositionType.POSITION_2);
        assertTrue(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.substitutePlayer(teamType, middleBlocker, PositionType.POSITION_5, ActionOriginType.USER);
        assertFalse(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        int playerAtPosition6 = composition.getPlayerAtPosition(PositionType.POSITION_6);
        int playerAtPosition3 = composition.getPlayerAtPosition(PositionType.POSITION_3);
        game.substitutePlayer(teamType, libero, PositionType.POSITION_6, ActionOriginType.USER);
        assertFalse(composition.isMiddleBlocker(middleBlocker));
        assertFalse(composition.isMiddleBlocker(oppositeMiddleBlocker));
        assertTrue(composition.isMiddleBlocker(playerAtPosition6));
        assertTrue(composition.isMiddleBlocker(playerAtPosition3));
    }

    @Test
    public void substitution_libero_singleLine_rotate() {
        IndoorGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "", System.currentTimeMillis(),
                                                       System.currentTimeMillis(), Rules.officialIndoorRules());
        createTeamWithNPlayers(game, TeamType.HOME, 10);
        createTeamWithNPlayers(game, TeamType.GUEST, 10);
        game.startMatch();

        TeamType teamType = TeamType.HOME;
        initCourt(game, teamType);

        IndoorSet set = (IndoorSet) game.currentSet();
        set.setServingTeamAtStart(TeamType.GUEST);
        IndoorTeamComposition composition = (IndoorTeamComposition) set.getTeamComposition(teamType);

        int middleBlocker = game.getPlayerAtPosition(teamType, PositionType.POSITION_5);
        int libero = 10;
        game.substitutePlayer(teamType, libero, PositionType.POSITION_5, ActionOriginType.USER);
        int oppositeMiddleBlocker = composition.getPlayerAtPosition(PositionType.POSITION_2);
        assertTrue(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.substitutePlayer(teamType, middleBlocker, PositionType.POSITION_5, ActionOriginType.USER);
        assertFalse(composition.isMiddleBlocker(middleBlocker));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.addPoint(TeamType.HOME);
        assertEquals(oppositeMiddleBlocker, composition.getPlayerAtPosition(PositionType.POSITION_1));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.addPoint(TeamType.GUEST);
        assertEquals(libero, composition.getPlayerAtPosition(PositionType.POSITION_1));
        assertTrue(composition.isMiddleBlocker(oppositeMiddleBlocker));

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);

        assertEquals(middleBlocker, composition.getPlayerAtPosition(PositionType.POSITION_1));
        assertEquals(PositionType.BENCH, composition.getPlayerPosition(libero));
        assertFalse(composition.isMiddleBlocker(middleBlocker));

        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);
        game.addPoint(TeamType.HOME);
        game.addPoint(TeamType.GUEST);

        assertEquals(libero, composition.getPlayerAtPosition(PositionType.POSITION_1));
    }
}
