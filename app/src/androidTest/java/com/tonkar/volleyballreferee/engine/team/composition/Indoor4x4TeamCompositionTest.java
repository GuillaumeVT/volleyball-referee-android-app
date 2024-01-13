package com.tonkar.volleyballreferee.engine.team.composition;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.definition.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class Indoor4x4TeamCompositionTest {

    @Test
    public void defaultTeam() {
        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(
                new IndoorTeamDefinition(GameType.INDOOR_4X4, UUID.randomUUID().toString(), "", TeamType.HOME), Rules.NO_LIMITATION, 4);

        assertEquals(0, team.getTeamDefinition().getNumberOfPlayers());
        assertEquals(0, team.getPlayersOnCourt().size());
        assertFalse(team.getTeamDefinition().hasPlayer(5));
        assertFalse(team.getTeamDefinition().hasPlayer(-1));
        assertNull(team.getPlayerPosition(5));
    }

    @Test
    public void createPlayers() {
        IndoorTeamDefinition teamDefinition = new IndoorTeamDefinition(GameType.INDOOR_4X4, UUID.randomUUID().toString(), "",
                                                                       TeamType.HOME);
        int playerCount = 7;

        for (int index = 1; index <= playerCount; index++) {
            teamDefinition.addPlayer(index);
            assertEquals(index, teamDefinition.getNumberOfPlayers());
            assertTrue(teamDefinition.hasPlayer(index));
        }

        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(teamDefinition, Rules.NO_LIMITATION, 4);
        assertEquals(0, team.getPlayersOnCourt().size());

        for (int index = 1; index <= playerCount; index++) {
            assertEquals(PositionType.BENCH, team.getPlayerPosition(index));
        }
    }

    private IndoorTeamDefinition createTeamWithNPlayers(int playerCount) {
        IndoorTeamDefinition teamDefinition = new IndoorTeamDefinition(GameType.INDOOR_4X4, UUID.randomUUID().toString(), "",
                                                                       TeamType.GUEST);

        for (int index = 1; index <= playerCount; index++) {
            teamDefinition.addPlayer(index);
        }

        return teamDefinition;
    }

    private Indoor4x4TeamComposition createTeamWithNPlayersAndFillCourt(int playerCount) {
        IndoorTeamDefinition teamDefinition = createTeamWithNPlayers(playerCount);
        teamDefinition.setCaptain(3);
        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(teamDefinition, Rules.NO_LIMITATION, 4);
        int playersOnCourt = 4;

        for (int index = 1; index <= playersOnCourt; index++) {
            team.substitutePlayer(index, PositionType.fromInt(index), ActionOriginType.USER);
        }

        return team;
    }

    @Test
    public void substitution_fillCourt() {
        IndoorTeamDefinition teamDefinition = createTeamWithNPlayers(10);
        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(teamDefinition, Rules.NO_LIMITATION, 4);
        int playerCount = 4;

        assertEquals(0, team.getPlayersOnCourt().size());

        for (int index = 1; index <= playerCount; index++) {
            assertTrue(team.substitutePlayer(index, PositionType.fromInt(index), ActionOriginType.USER));
            assertEquals(PositionType.fromInt(index), team.getPlayerPosition(index));
            assertEquals(index, team.getPlayersOnCourt().size());
        }

    }

    @Test
    public void substitution_changePlayer_free() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(7);
        assertTrue(team.substitutePlayer(5, PositionType.POSITION_4, 0, 0, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));
        assertTrue(team.substitutePlayer(4, PositionType.POSITION_2, 0, 0, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(2));
        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_1);
        assertEquals(3, availablePlayers.size());
        assertTrue(availablePlayers.contains(2));
        assertTrue(availablePlayers.contains(6));
        assertTrue(availablePlayers.contains(7));
    }

    @Test
    public void substitution_changePlayer_confirmed() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(8);
        team.confirmStartingLineup();
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_4, 0, 0, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(4));
        assertTrue(availablePlayers.contains(5));
        assertTrue(availablePlayers.contains(6));
        assertTrue(availablePlayers.contains(8));

        assertTrue(team.substitutePlayer(4, PositionType.POSITION_2, 0, 0, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(2));
    }

    @Test
    public void substitution_changePlayer_max() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(9);
        team.confirmStartingLineup();

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_1, 0, 0, ActionOriginType.USER));
        assertTrue(team.substitutePlayer(1, PositionType.POSITION_1, 0, 0, ActionOriginType.USER));
        assertTrue(team.substitutePlayer(8, PositionType.POSITION_2, 0, 0, ActionOriginType.USER));
        assertTrue(team.substitutePlayer(2, PositionType.POSITION_2, 0, 0, ActionOriginType.USER));
        assertFalse(team.substitutePlayer(5, PositionType.POSITION_4, 0, 0, ActionOriginType.USER));
    }

    @Test
    public void rotation_next() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(5);
        team.confirmStartingLineup();

        team.rotateToNextPositions();
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(4));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(5));
    }

    @Test
    public void rotation_previous() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(5);
        team.confirmStartingLineup();

        team.rotateToPreviousPositions();
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(4));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(5));
    }

    @Test
    public void captain_definition() {
        IndoorTeamDefinition teamDefinition = createTeamWithNPlayers(6);

        teamDefinition.setCaptain(5);
        assertEquals(5, teamDefinition.getCaptain());
    }

    @Test
    public void captain_composition() {
        Indoor4x4TeamComposition teamComposition = createTeamWithNPlayersAndFillCourt(6);

        assertFalse(teamComposition.hasGameCaptainOnCourt());
        teamComposition.confirmStartingLineup();
        assertTrue(teamComposition.hasGameCaptainOnCourt());

        int captain = 3;
        assertEquals(captain, teamComposition.getGameCaptain());

        int secondaryCaptain = 5;
        teamComposition.substitutePlayer(secondaryCaptain, PositionType.POSITION_3, 0, 0, ActionOriginType.USER);
        assertFalse(teamComposition.hasGameCaptainOnCourt());

        teamComposition.setGameCaptain(secondaryCaptain);
        assertEquals(secondaryCaptain, teamComposition.getGameCaptain());
        assertFalse(teamComposition.isGameCaptain(captain));
        assertTrue(teamComposition.isGameCaptain(secondaryCaptain));
        assertTrue(teamComposition.hasGameCaptainOnCourt());

        teamComposition.substitutePlayer(captain, PositionType.POSITION_3, 0, 0, ActionOriginType.USER);
        assertEquals(captain, teamComposition.getGameCaptain());
        assertTrue(teamComposition.isGameCaptain(captain));
        assertFalse(teamComposition.isGameCaptain(secondaryCaptain));
        assertTrue(teamComposition.hasGameCaptainOnCourt());
    }
}
