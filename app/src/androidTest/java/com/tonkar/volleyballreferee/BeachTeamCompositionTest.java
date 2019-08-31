package com.tonkar.volleyballreferee;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.BeachTeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.BeachTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BeachTeamCompositionTest {

    @Test
    public void defaultTeam() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(UUID.randomUUID().toString(), "", TeamType.GUEST));

        assertEquals(2, team.getTeamDefinition().getNumberOfPlayers());
        assertTrue(team.getTeamDefinition().hasPlayer(1));
        assertTrue(team.getTeamDefinition().hasPlayer(2));
        assertFalse(team.getTeamDefinition().hasPlayer(5));
        assertFalse(team.getTeamDefinition().hasPlayer(-1));
        assertNull(team.getPlayerPosition(5));
        assertNull(team.getPlayerPosition(-1));
        assertEquals(2, team.getPlayersOnCourt().size());
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }

    @Test
    public void substitution_changePlayer() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(UUID.randomUUID().toString(), "", TeamType.HOME));

        assertTrue(team.substitutePlayer(1, PositionType.POSITION_2));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(1));
        assertEquals(1, team.getPlayersOnCourt().size());

        assertTrue(team.substitutePlayer(2, PositionType.POSITION_1));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(2));
        assertEquals(2, team.getPlayersOnCourt().size());
    }

    @Test
    public void substitution_abnormal() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(UUID.randomUUID().toString(), "", TeamType.HOME));

        assertFalse(team.substitutePlayer(5, PositionType.POSITION_1));
        assertFalse(team.substitutePlayer(1, PositionType.POSITION_5));
        assertFalse(team.substitutePlayer(3, PositionType.POSITION_6));
    }

    @Test
    public void rotation_next() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(UUID.randomUUID().toString(), "", TeamType.HOME));
        team.substitutePlayer(1, PositionType.POSITION_2);
        team.substitutePlayer(2, PositionType.POSITION_1);

        team.rotateToNextPositions();
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }

    @Test
    public void rotation_previous() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(UUID.randomUUID().toString(), "", TeamType.HOME));
        team.substitutePlayer(1, PositionType.POSITION_2);
        team.substitutePlayer(2, PositionType.POSITION_1);

        team.rotateToPreviousPositions();
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }
}
