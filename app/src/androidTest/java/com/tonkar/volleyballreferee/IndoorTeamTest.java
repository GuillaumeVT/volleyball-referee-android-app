package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.team.IndoorTeam;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class IndoorTeamTest {

    private final PositionType[] positions = { PositionType.POSITION_1, PositionType.POSITION_2, PositionType.POSITION_3, PositionType.POSITION_4, PositionType.POSITION_5, PositionType.POSITION_6 };

    @Test
    public void defaultTeam() {
        IndoorTeam team = new IndoorTeam(TeamType.HOME, 6);

        assertEquals(0, team.getNumberOfPlayers());
        assertEquals(0, team.getPlayersOnCourt().size());
        assertEquals(false, team.hasPlayer(5));
        assertEquals(false, team.hasPlayer(-1));
        assertEquals(null, team.getPlayerPosition(5));
    }

    @Test
    public void createPlayers() {
        IndoorTeam team = new IndoorTeam(TeamType.HOME, 6);
        int playerCount = 7;

        for (int index = 1; index <= playerCount; index++) {
            team.addPlayer(index);
            assertEquals(index, team.getNumberOfPlayers());
            assertEquals(0, team.getPlayersOnCourt().size());
            assertEquals(true, team.hasPlayer(index));
            assertEquals(PositionType.BENCH, team.getPlayerPosition(index));
        }
    }

    private IndoorTeam createTeamWith10Players() {
        IndoorTeam team = new IndoorTeam(TeamType.GUEST, 6);
        int playerCount = 10;

        for (int index = 1; index <= playerCount; index++) {
            team.addPlayer(index);
        }

        return team;
    }

    private IndoorTeam createTeamWith10PlayersAndFillCourt() {
        IndoorTeam team = createTeamWith10Players();
        int playersOnCourt = 6;

        for (int index = 1; index <= playersOnCourt; index++) {
            team.substitutePlayer(index, positions[index-1]);
        }

        return team;
    }

    @Test
    public void substitution_fillCourt() {
        IndoorTeam team = createTeamWith10Players();
        int playerCount = 6;

        assertEquals(0, team.getPlayersOnCourt().size());

        for (int index = 1; index <= playerCount; index++) {
            assertEquals(true, team.substitutePlayer(index, positions[index-1]));
            assertEquals(positions[index-1], team.getPlayerPosition(index));
            assertEquals(index, team.getPlayersOnCourt().size());
        }

    }

    @Test
    public void substitution_changePlayer_free() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_4));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));
        assertEquals(true, team.substitutePlayer(4, PositionType.POSITION_6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));
        List<Integer> availablePlayers = team.getPossibleReplacements(PositionType.POSITION_1);
        assertEquals(4, availablePlayers.size());
        assertEquals(true, availablePlayers.contains(6));
        assertEquals(true, availablePlayers.contains(8));
        assertEquals(true, availablePlayers.contains(9));
        assertEquals(true, availablePlayers.contains(10));
    }

    @Test
    public void substitution_changePlayer_confirmed_front() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.confirmStartingLineup();
        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_4));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        List<Integer> availablePlayers = team.getPossibleReplacements(PositionType.POSITION_4);
        assertEquals(1, availablePlayers.size());
        assertEquals(true, availablePlayers.contains(4));

        assertEquals(false, team.substitutePlayer(4, PositionType.POSITION_6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        assertEquals(true, team.substitutePlayer(4, PositionType.POSITION_4));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleReplacements(PositionType.POSITION_4);
        assertEquals(0, availablePlayers.size());

        assertEquals(false, team.substitutePlayer(7, PositionType.POSITION_4));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        team.addLibero(10);
        availablePlayers = team.getPossibleReplacements(PositionType.POSITION_4);
        assertEquals(0, availablePlayers.size());
    }

    @Test
    public void substitution_changePlayer_confirmed_back() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.confirmStartingLineup();
        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));

        List<Integer> availablePlayers = team.getPossibleReplacements(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());
        assertEquals(true, availablePlayers.contains(6));

        assertEquals(false, team.substitutePlayer(6, PositionType.POSITION_3));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));

        assertEquals(true, team.substitutePlayer(6, PositionType.POSITION_6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleReplacements(PositionType.POSITION_6);
        assertEquals(0, availablePlayers.size());

        assertEquals(false, team.substitutePlayer(7, PositionType.POSITION_6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        team.addLibero(10);
        availablePlayers = team.getPossibleReplacements(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());
        assertEquals(true, availablePlayers.contains(10));
    }

    @Test
    public void substitution_changePlayer_libero() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.confirmStartingLineup();

        team.addLibero(10);
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_6).contains(10));
        assertEquals(false, team.getPossibleReplacements(PositionType.POSITION_4).contains(10));
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_6).size() > 1);

        assertEquals(true, team.substitutePlayer(10, PositionType.POSITION_5));

        assertEquals(1, team.getPossibleReplacements(PositionType.POSITION_5).size());
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_5).contains(5));
        assertEquals(false, team.getPossibleReplacements(PositionType.POSITION_6).contains(10));

        assertEquals(true, team.substitutePlayer(5, PositionType.POSITION_5));
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_6).contains(10));
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_6).size() > 1);

        team.addLibero(11);
        assertEquals(false, team.getPossibleReplacements(PositionType.POSITION_6).contains(11));
        assertEquals(false, team.getPossibleReplacements(PositionType.POSITION_4).contains(11));

        team.addLibero(9);
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_6).contains(9));
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_6).contains(10));
        assertEquals(false, team.getPossibleReplacements(PositionType.POSITION_4).contains(9));

        team.substitutePlayer(10, PositionType.POSITION_5);
        assertEquals(2, team.getPossibleReplacements(PositionType.POSITION_5).size());
        assertEquals(true, team.getPossibleReplacements(PositionType.POSITION_5).contains(5));
        assertEquals(false, team.getPossibleReplacements(PositionType.POSITION_5).contains(11));
    }

    @Test
    public void substitution_changePlayer_max() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.confirmStartingLineup();

        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_1));
        assertEquals(true, team.substitutePlayer(1, PositionType.POSITION_1));
        assertEquals(true, team.substitutePlayer(8, PositionType.POSITION_2));
        assertEquals(true, team.substitutePlayer(2, PositionType.POSITION_2));
        assertEquals(true, team.substitutePlayer(9, PositionType.POSITION_3));
        assertEquals(true, team.substitutePlayer(3, PositionType.POSITION_3));
        assertEquals(false, team.substitutePlayer(10, PositionType.POSITION_4));
    }

    @Test
    public void substitution_abnormal() {
        IndoorTeam team = createTeamWith10Players();
        assertEquals(false, team.substitutePlayer(18, PositionType.POSITION_1));

        team.addLibero(10);
        assertEquals(false, team.substitutePlayer(10, PositionType.POSITION_4));
    }

    @Test
    public void rotation_next() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.confirmStartingLineup();

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
    public void rotation_next_libero() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.addLibero(10);
        team.confirmStartingLineup();
        assertEquals(true, team.substitutePlayer(10, PositionType.POSITION_6));

        team.rotateToNextPositions();
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(4));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(5));
        assertEquals(PositionType.POSITION_5, team.getPlayerPosition(10));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        team.rotateToNextPositions();
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(4));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(5));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(6));
        assertEquals(PositionType.POSITION_5, team.getPlayerPosition(1));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        assertEquals(10, team.checkPosition1Defence());
        assertEquals(-1, team.checkPosition1Offence());

        assertEquals(true, team.substitutePlayer(10, PositionType.POSITION_1));

        assertEquals(-1, team.checkPosition1Defence());
        assertEquals(3, team.checkPosition1Offence());
    }

    @Test
    public void rotation_previous() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.confirmStartingLineup();

        team.rotateToPreviousPositions();
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_5, team.getPlayerPosition(4));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(5));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));
    }

    @Test
    public void rotation_previous_libero() {
        IndoorTeam team = createTeamWith10PlayersAndFillCourt();
        team.addLibero(10);
        team.confirmStartingLineup();

        assertEquals(true, team.substitutePlayer(10, PositionType.POSITION_1));

        team.rotateToPreviousPositions();
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_4, team.getPlayerPosition(3));
        assertEquals(PositionType.POSITION_5, team.getPlayerPosition(10));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(5));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(6));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));
    }

}
