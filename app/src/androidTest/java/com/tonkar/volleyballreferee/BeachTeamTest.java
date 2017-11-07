package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.team.BeachTeam;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BeachTeamTest {

    @Test
    public void defaultTeam() {
        BeachTeam team = new BeachTeam(TeamType.GUEST);

        assertEquals(2, team.getNumberOfPlayers());
        assertEquals(true, team.hasPlayer(1));
        assertEquals(true, team.hasPlayer(2));
        assertEquals(false, team.hasPlayer(5));
        assertEquals(false, team.hasPlayer(-1));
        assertEquals(null, team.getPlayerPosition(5));
        assertEquals(null, team.getPlayerPosition(-1));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(1));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(2));
    }

    @Test
    public void substitution_fillCourt() {
        BeachTeam team = new BeachTeam(TeamType.HOME);

        assertEquals(2, team.getPlayersOnBench().size());
        assertEquals(0, team.getPlayersOnCourt().size());

        assertEquals(true, team.substitutePlayer(1, PositionType.POSITION_2));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(1));
        assertEquals(1, team.getPlayersOnBench().size());
        assertEquals(1, team.getPlayersOnCourt().size());

        assertEquals(true, team.substitutePlayer(2, PositionType.POSITION_1));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(2));
        assertEquals(0, team.getPlayersOnBench().size());
        assertEquals(2, team.getPlayersOnCourt().size());
    }

    @Test
    public void substitution_changePlayer() {
        BeachTeam team = new BeachTeam(TeamType.HOME);

        team.substitutePlayer(1, PositionType.POSITION_1);
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));

        assertEquals(true, team.substitutePlayer(2, PositionType.POSITION_1));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(2));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(1));

        assertEquals(1, team.getPlayersOnBench().size());
        assertEquals(1, team.getPlayersOnCourt().size());
    }

    @Test
    public void substitution_abnormal() {
        BeachTeam team = new BeachTeam(TeamType.HOME);

        assertEquals(false, team.substitutePlayer(5, PositionType.POSITION_1));
        assertEquals(false, team.substitutePlayer(1, PositionType.POSITION_5));
        assertEquals(false, team.substitutePlayer(3, PositionType.POSITION_6));
    }

    @Test
    public void rotation_next() {
        BeachTeam team = new BeachTeam(TeamType.HOME);
        team.substitutePlayer(1, PositionType.POSITION_2);
        team.substitutePlayer(2, PositionType.POSITION_1);

        team.rotateToNextPositions();
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }

    @Test
    public void rotation_previous() {
        BeachTeam team = new BeachTeam(TeamType.HOME);
        team.substitutePlayer(1, PositionType.POSITION_2);
        team.substitutePlayer(2, PositionType.POSITION_1);

        team.rotateToPreviousPositions();
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
    }
}
