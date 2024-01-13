package com.tonkar.volleyballreferee.engine.team.substitution;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.IndoorTeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class IndoorTeamSubstitutionsLimitationsTest {

    private IndoorTeamDefinition createTeamWithNPlayers(int playerCount) {
        IndoorTeamDefinition teamDefinition = new IndoorTeamDefinition(GameType.INDOOR, UUID.randomUUID().toString(), "", TeamType.GUEST);

        for (int index = 1; index <= playerCount; index++) {
            teamDefinition.addPlayer(index);
        }

        return teamDefinition;
    }

    private IndoorTeamComposition createTeamWithNPlayersAndFillCourt(int playerCount, int substitutionType) {
        IndoorTeamDefinition teamDefinition = createTeamWithNPlayers(playerCount);
        teamDefinition.setCaptain(3);
        IndoorTeamComposition team = new IndoorTeamComposition(teamDefinition, substitutionType, 12);
        int playersOnCourt = 6;

        for (int index = 1; index <= playersOnCourt; index++) {
            team.substitutePlayer(index, PositionType.fromInt(index), ActionOriginType.USER);
        }

        return team;
    }

    @Test
    public void substitution_fillCourt_alternativeLimitation1() {
        substitution_fillCourt(Rules.ALTERNATIVE_LIMITATION_1);
    }

    @Test
    public void substitution_fillCourt_alternativeLimitation2() {
        substitution_fillCourt(Rules.ALTERNATIVE_LIMITATION_2);
    }

    @Test
    public void substitution_fillCourt_free() {
        substitution_fillCourt(Rules.NO_LIMITATION);
    }

    private void substitution_fillCourt(int substitutionType) {
        IndoorTeamDefinition teamDefinition = createTeamWithNPlayers(10);
        IndoorTeamComposition team = new IndoorTeamComposition(teamDefinition, substitutionType, 6);
        int playerCount = 6;

        assertEquals(0, team.getPlayersOnCourt().size());

        for (int index = 1; index <= playerCount; index++) {
            assertTrue(team.substitutePlayer(index, PositionType.fromInt(index), ActionOriginType.USER));
            assertEquals(PositionType.fromInt(index), team.getPlayerPosition(index));
            assertEquals(index, team.getPlayersOnCourt().size());
        }

    }

    @Test
    public void substitution_changePlayer_noLimitation_alternativeLimitation1() {
        substitution_changePlayer_free(Rules.ALTERNATIVE_LIMITATION_1);
    }

    @Test
    public void substitution_changePlayer_noLimitation_alternativeLimitation2() {
        substitution_changePlayer_free(Rules.ALTERNATIVE_LIMITATION_2);
    }

    @Test
    public void substitution_changePlayer_free_noLimitation() {
        substitution_changePlayer_free(Rules.NO_LIMITATION);
    }

    private void substitution_changePlayer_free(int substitutionType) {
        IndoorTeamComposition team = createTeamWithNPlayersAndFillCourt(10, substitutionType);
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));
        assertTrue(team.substitutePlayer(4, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));
        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_1);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(6));
        assertTrue(availablePlayers.contains(8));
        assertTrue(availablePlayers.contains(9));
        assertTrue(availablePlayers.contains(10));
    }

    @Test
    public void substitution_changePlayer_confirmed_front_alternativeLimitation1() {
        IndoorTeamComposition team = createTeamWithNPlayersAndFillCourt(11, Rules.ALTERNATIVE_LIMITATION_1);
        team.confirmStartingLineup();
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(1, availablePlayers.size());
        assertTrue(availablePlayers.contains(4));

        assertFalse(team.substitutePlayer(4, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        assertTrue(team.substitutePlayer(4, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(1, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        assertFalse(team.substitutePlayer(8, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(8));

        assertTrue(team.substitutePlayer(4, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(1, availablePlayers.size());
    }

    @Test
    public void substitution_changePlayer_confirmed_front_alternativeLimitation2() {
        IndoorTeamComposition team = createTeamWithNPlayersAndFillCourt(10, Rules.ALTERNATIVE_LIMITATION_2);
        team.confirmStartingLineup();
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(4));

        assertFalse(team.substitutePlayer(4, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        assertTrue(team.substitutePlayer(4, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(4, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        IndoorTeamDefinition teamDefinition = (IndoorTeamDefinition) team.getTeamDefinition();
        teamDefinition.addLibero(10);
        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(3, availablePlayers.size());

        assertTrue(team.substitutePlayer(8, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(3, availablePlayers.size());

        assertTrue(team.substitutePlayer(4, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(8));

        assertTrue(team.substitutePlayer(9, PositionType.POSITION_3, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(3));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(2, availablePlayers.size());

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_3);
        assertEquals(1, availablePlayers.size());

        assertTrue(team.substitutePlayer(3, PositionType.POSITION_3, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(9));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_3);
        assertEquals(1, availablePlayers.size());
    }

    @Test
    public void substitution_changePlayer_confirmed_front_noLimitation() {
        IndoorTeamComposition team = createTeamWithNPlayersAndFillCourt(10, Rules.NO_LIMITATION);
        team.confirmStartingLineup();
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(4));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(4));

        assertTrue(team.substitutePlayer(4, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(4));

        assertTrue(team.substitutePlayer(6, PositionType.POSITION_4, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(4, availablePlayers.size());

        assertFalse(team.substitutePlayer(2, PositionType.POSITION_3, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_2, team.getPlayerPosition(2));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(3));
    }

    @Test
    public void substitution_changePlayer_confirmed_back_alternativeLimitation1() {
        IndoorTeamComposition team = createTeamWithNPlayersAndFillCourt(10, Rules.ALTERNATIVE_LIMITATION_1);
        team.confirmStartingLineup();
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());
        assertTrue(availablePlayers.contains(6));

        assertFalse(team.substitutePlayer(6, PositionType.POSITION_3, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));

        assertTrue(team.substitutePlayer(6, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(7));

        IndoorTeamDefinition teamDefinition = (IndoorTeamDefinition) team.getTeamDefinition();
        teamDefinition.addLibero(10);
        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(2, availablePlayers.size());
        assertTrue(availablePlayers.contains(10));

        assertTrue(team.substitutePlayer(10, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(10));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(2, availablePlayers.size());

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_5);
        assertEquals(3, availablePlayers.size());

        assertTrue(team.substitutePlayer(10, PositionType.POSITION_5, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_5, team.getPlayerPosition(10));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());
    }

    @Test
    public void substitution_changePlayer_confirmed_back_alternativeLimitation2() {
        IndoorTeamComposition team = createTeamWithNPlayersAndFillCourt(10, Rules.ALTERNATIVE_LIMITATION_2);
        team.confirmStartingLineup();
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(6));

        assertFalse(team.substitutePlayer(6, PositionType.POSITION_3, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));

        assertTrue(team.substitutePlayer(6, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(7));

        IndoorTeamDefinition teamDefinition = (IndoorTeamDefinition) team.getTeamDefinition();
        teamDefinition.addLibero(10);
        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(10));

        assertTrue(team.substitutePlayer(10, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(10));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_5);
        assertEquals(3, availablePlayers.size());
    }

    @Test
    public void substitution_changePlayer_confirmed_back_noLimitation() {
        IndoorTeamComposition team = createTeamWithNPlayersAndFillCourt(10, Rules.NO_LIMITATION);
        team.confirmStartingLineup();
        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(6));

        Set<Integer> availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(6));

        assertTrue(team.substitutePlayer(6, PositionType.POSITION_3, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_3, team.getPlayerPosition(6));

        assertTrue(team.substitutePlayer(3, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.BENCH, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(7));

        IndoorTeamDefinition teamDefinition = (IndoorTeamDefinition) team.getTeamDefinition();
        teamDefinition.addLibero(10);
        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());
        assertTrue(availablePlayers.contains(10));

        assertTrue(team.substitutePlayer(10, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(10));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(1, availablePlayers.size());

        assertTrue(team.substitutePlayer(7, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(7));

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_6);
        assertEquals(4, availablePlayers.size());

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_5);
        assertEquals(4, availablePlayers.size());

        availablePlayers = team.getPossibleSubstitutions(PositionType.POSITION_4);
        assertEquals(3, availablePlayers.size());

        assertFalse(team.substitutePlayer(1, PositionType.POSITION_6, ActionOriginType.USER));
        assertEquals(PositionType.POSITION_1, team.getPlayerPosition(1));
        assertEquals(PositionType.POSITION_6, team.getPlayerPosition(7));
    }
}
