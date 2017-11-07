package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.team.IndoorTeam;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class IndoorTeamTest {

    private final PositionType[] positions = { PositionType.POSITION_1, PositionType.POSITION_2, PositionType.POSITION_3, PositionType.POSITION_4, PositionType.POSITION_5, PositionType.POSITION_6 };

    @Test
    public void defaultTeam() {
        IndoorTeam team = new IndoorTeam(TeamType.HOME);

        assertEquals(0, team.getNumberOfPlayers());
        assertEquals(0, team.getPlayersOnBench().size());
        assertEquals(0, team.getPlayersOnCourt().size());
        assertEquals(false, team.hasPlayer(5));
        assertEquals(false, team.hasPlayer(-1));
        assertEquals(null, team.getPlayerPosition(5));
    }

    @Test
    public void createPlayers() {
        IndoorTeam team = new IndoorTeam(TeamType.HOME);
        int playerCount = 7;

        for (int index = 1; index <= playerCount; index++) {
            team.addPlayer(index);
            assertEquals(index, team.getNumberOfPlayers());
            assertEquals(index, team.getPlayersOnBench().size());
            assertEquals(0, team.getPlayersOnCourt().size());
            assertEquals(true, team.hasPlayer(index));
            assertEquals(PositionType.BENCH, team.getPlayerPosition(index));
        }
    }

    private IndoorTeam createTeamWith7Players() {
        IndoorTeam team = new IndoorTeam(TeamType.GUEST);
        int playerCount = 7;

        for (int index = 1; index <= playerCount; index++) {
            team.addPlayer(index);
        }

        return team;
    }

    private IndoorTeam createTeamWith7PlayersAndFillCourt() {
        IndoorTeam team = createTeamWith7Players();
        int playersOnCourt = 6;

        for (int index = 1; index <= playersOnCourt; index++) {
            team.substitutePlayer(index, positions[index-1]);
        }

        return team;
    }

    @Test
    public void substitution_fillCourt() {
        IndoorTeam team = createTeamWith7Players();
        int playerCount = 7;

        assertEquals(playerCount, team.getPlayersOnBench().size());
        assertEquals(0, team.getPlayersOnCourt().size());

        for (int index = 1; index < playerCount; index++) {
            assertEquals(true, team.substitutePlayer(index, positions[index-1]));
            assertEquals(positions[index-1], team.getPlayerPosition(index));
            assertEquals(playerCount - index, team.getPlayersOnBench().size());
            assertEquals(index, team.getPlayersOnCourt().size());
        }

    }

    @Test
    public void substitution_changePlayer() {
        IndoorTeam team = createTeamWith7PlayersAndFillCourt();

        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_4));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(7));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));
        assertEquals(1, team.getPlayersOnBench().size());
        assertEquals(6, team.getPlayersOnCourt().size());

        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_3));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(7));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(3));
        assertEquals(2, team.getPlayersOnBench().size());
        assertEquals(5, team.getPlayersOnCourt().size());
    }

    @Test
    public void substitution_abnormal() {
        IndoorTeam team = createTeamWith7Players();
        assertEquals(false, team.substitutePlayer(8, PositionType.POSITION_1));
    }

    @Test
    public void rotation_next() {
        IndoorTeam team = createTeamWith7PlayersAndFillCourt();

        team.rotateToNextPositions();
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(4));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(5));
        assertEquals(PositionType.POSITION_5, team.getPlayerPosition(6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));
    }

    @Test
    public void rotation_previous() {
        IndoorTeam team = createTeamWith7PlayersAndFillCourt();

        team.rotateToPreviousPositions();
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_5, team.getPlayerPosition(4));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(5));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));
    }

}
