package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.team.BeachTeamComposition;
import com.tonkar.volleyballreferee.business.team.BeachTeamDefinition;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BeachTeamCompositionTest {

    @Test
    public void defaultTeam() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(TeamType.GUEST));

        assertEquals(2, team.getTeamDefinition().getNumberOfPlayers());
        assertEquals(true, team.getTeamDefinition().hasPlayer(1));
        assertEquals(true, team.getTeamDefinition().hasPlayer(2));
        assertEquals(false, team.getTeamDefinition().hasPlayer(5));
        assertEquals(false, team.getTeamDefinition().hasPlayer(-1));
        assertEquals(null, team.getPlayerPosition(5));
        assertEquals(null, team.getPlayerPosition(-1));
        assertEquals(2, team.getPlayersOnCourt().size());
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }

    @Test
    public void substitution_changePlayer() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(TeamType.HOME));

        assertEquals(true, team.substitutePlayer(1, PositionType.POSITION_2));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(1));
        assertEquals(1, team.getPlayersOnCourt().size());

        assertEquals(true, team.substitutePlayer(2, PositionType.POSITION_1));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(2));
        assertEquals(2, team.getPlayersOnCourt().size());
    }

    @Test
    public void substitution_abnormal() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(TeamType.HOME));

        assertEquals(false, team.substitutePlayer(5, PositionType.POSITION_1));
        assertEquals(false, team.substitutePlayer(1, PositionType.POSITION_5));
        assertEquals(false, team.substitutePlayer(3, PositionType.POSITION_6));
    }

    @Test
    public void rotation_next() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(TeamType.HOME));
        team.substitutePlayer(1, PositionType.POSITION_2);
        team.substitutePlayer(2, PositionType.POSITION_1);

        team.rotateToNextPositions();
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }

    @Test
    public void rotation_previous() {
        BeachTeamComposition team = new BeachTeamComposition(new BeachTeamDefinition(TeamType.HOME));
        team.substitutePlayer(1, PositionType.POSITION_2);
        team.substitutePlayer(2, PositionType.POSITION_1);

        team.rotateToPreviousPositions();
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }
}
