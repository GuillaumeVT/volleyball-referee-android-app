package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.team.Indoor4x4TeamComposition;
import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class Indoor4x4TeamCompositionTest {

    @Test
    public void defaultTeam() {
        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(new IndoorTeamDefinition(TeamType.HOME), 4);

        assertEquals(0, team.getTeamDefinition().getNumberOfPlayers());
        assertEquals(0, team.getPlayersOnCourt().size());
        assertEquals(false, team.getTeamDefinition().hasPlayer(5));
        assertEquals(false, team.getTeamDefinition().hasPlayer(-1));
        assertEquals(null, team.getPlayerPosition(5));
    }

    @Test
    public void createPlayers() {
        IndoorTeamDefinition teamDefinition = new IndoorTeamDefinition(TeamType.HOME);
        int playerCount = 7;

        for (int index = 1; index <= playerCount; index++) {
            teamDefinition.addPlayer(index);
            assertEquals(index, teamDefinition.getNumberOfPlayers());
            assertEquals(true, teamDefinition.hasPlayer(index));
        }

        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(teamDefinition, 4);
        assertEquals(0, team.getPlayersOnCourt().size());

        for (int index = 1; index <= playerCount; index++) {
            assertEquals(PositionType.BENCH, team.getPlayerPosition(index));
        }
    }

    private IndoorTeamDefinition createTeamWithNPlayers(int playerCount) {
        IndoorTeamDefinition teamDefinition = new IndoorTeamDefinition(TeamType.GUEST);

        for (int index = 1; index <= playerCount; index++) {
            teamDefinition.addPlayer(index);
        }

        return teamDefinition;
    }

    private Indoor4x4TeamComposition createTeamWithNPlayersAndFillCourt(int playerCount) {
        IndoorTeamDefinition teamDefinition = createTeamWithNPlayers(playerCount);
        teamDefinition.setCaptain(3);
        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(teamDefinition, 4);
        int playersOnCourt = 4;

        for (int index = 1; index <= playersOnCourt; index++) {
            team.substitutePlayer(index, PositionType.fromInt(index));
        }

        return team;
    }

    @Test
    public void substitution_fillCourt() {
        IndoorTeamDefinition teamDefinition = createTeamWithNPlayers(10);
        Indoor4x4TeamComposition team = new Indoor4x4TeamComposition(teamDefinition, 4);
        int playerCount = 4;

        assertEquals(0, team.getPlayersOnCourt().size());

        for (int index = 1; index <= playerCount; index++) {
            assertEquals(true, team.substitutePlayer(index, PositionType.fromInt(index)));
            assertEquals(PositionType.fromInt(index), team.getPlayerPosition(index));
            assertEquals(index, team.getPlayersOnCourt().size());
        }

    }

    @Test
    public void substitution_changePlayer_free() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(7);
        assertEquals(true, team.substitutePlayer(5, PositionType.POSITION_4, 0, 0, false));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));
        assertEquals(true, team.substitutePlayer(4, PositionType.POSITION_2, 0, 0, true));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(2));
        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_1, false);
        assertEquals(3, availablePlayers.size());
        assertEquals(true, availablePlayers.contains(2));
        assertEquals(true, availablePlayers.contains(6));
        assertEquals(true, availablePlayers.contains(7));
    }

    @Test
    public void substitution_changePlayer_confirmed() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(8);
        team.confirmStartingLineup();
        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_4, 0, 0, false));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4,false);
        assertEquals(4, availablePlayers.size());
        assertEquals(true, availablePlayers.contains(4));
        assertEquals(true, availablePlayers.contains(5));
        assertEquals(true, availablePlayers.contains(6));
        assertEquals(true, availablePlayers.contains(8));

        assertEquals(true, team.substitutePlayer(4, PositionType.POSITION_2, 0, 0, false));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(2));
    }

    @Test
    public void substitution_changePlayer_max() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(9);
        team.confirmStartingLineup();

        assertEquals(true, team.substitutePlayer(7, PositionType.POSITION_1, 0, 0, false));
        assertEquals(true, team.substitutePlayer(1, PositionType.POSITION_1, 0, 0, false));
        assertEquals(true, team.substitutePlayer(8, PositionType.POSITION_2, 0, 0, false));
        assertEquals(true, team.substitutePlayer(2, PositionType.POSITION_2, 0, 0, false));
        assertEquals(false, team.substitutePlayer(5, PositionType.POSITION_4, 0, 0, false));
    }

    @Test
    public void substitution_abnormal() {
        Indoor4x4TeamComposition team = createTeamWithNPlayersAndFillCourt(8);
        assertEquals(false, team.substitutePlayer(18, PositionType.POSITION_1, 0, 0, false));

        assertEquals(false, team.substitutePlayer(7, PositionType.POSITION_6, 0, 0, false));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));
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

        assertEquals(false, teamComposition.hasActingCaptainOnCourt());
        teamComposition.confirmStartingLineup();
        assertEquals(true, teamComposition.hasActingCaptainOnCourt());

        int captain = 3;
        assertEquals(captain, teamComposition.getActingCaptain());

        teamComposition.substitutePlayer(5, PositionType.POSITION_3, 0, 0, false);
        assertEquals(false, teamComposition.hasActingCaptainOnCourt());

        teamComposition.setActingCaptain(5);
        assertEquals(5, teamComposition.getActingCaptain());
        assertEquals(true, teamComposition.hasActingCaptainOnCourt());

        teamComposition.substitutePlayer(captain, PositionType.POSITION_3, 0, 0, false);
        assertEquals(captain, teamComposition.getActingCaptain());
        assertEquals(true, teamComposition.hasActingCaptainOnCourt());
    }
}
