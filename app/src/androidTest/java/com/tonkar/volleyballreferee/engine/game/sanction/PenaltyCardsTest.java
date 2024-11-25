package com.tonkar.volleyballreferee.engine.game.sanction;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.api.model.SanctionDto;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class PenaltyCardsTest {

    @Test
    public void beachGame_delayWarning() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertEquals(SanctionType.DELAY_WARNING, game.getPossibleDelaySanction(TeamType.HOME));

        game.giveSanction(TeamType.HOME, SanctionType.DELAY_WARNING, SanctionDto.TEAM);

        assertTrue(game.hasSanctions(TeamType.HOME, SanctionDto.TEAM));
        assertEquals(SanctionType.DELAY_WARNING, game.getAllSanctions(TeamType.HOME).get(0).getCard());
        assertEquals(SanctionType.DELAY_PENALTY, game.getPossibleDelaySanction(TeamType.HOME));
    }

    @Test
    public void beachGame_delayPenalty() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertEquals(SanctionType.DELAY_WARNING, game.getPossibleDelaySanction(TeamType.GUEST));

        game.giveSanction(TeamType.GUEST, SanctionType.DELAY_PENALTY, SanctionDto.TEAM);

        assertTrue(game.hasSanctions(TeamType.GUEST, SanctionDto.TEAM));
        assertEquals(SanctionType.DELAY_PENALTY, game.getAllSanctions(TeamType.GUEST).get(0).getCard());
        assertEquals(SanctionType.DELAY_PENALTY, game.getPossibleDelaySanction(TeamType.GUEST));
    }

    @Test
    public void beachGame_yellowCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, 1);

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertEquals(SanctionType.YELLOW, game.getAllSanctions(TeamType.HOME).get(0).getCard());
        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));
    }

    @Test
    public void beachGame_redCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.RED, 1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertFalse(game.hasSanctions(TeamType.HOME, 5));
        assertEquals(SanctionType.RED, game.getAllSanctions(TeamType.HOME).get(0).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.RED, 1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(2, game.getPoints(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertFalse(game.hasSanctions(TeamType.HOME, 2));
        assertEquals(SanctionType.RED, game.getAllSanctions(TeamType.HOME).get(1).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));
    }

    @Test
    public void beachGame_redExpulsionCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 2);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, 2));
        assertEquals(SanctionType.RED_EXPULSION, game.getAllSanctions(TeamType.GUEST).get(0).getCard());
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED_DISQUALIFICATION));
    }

    @Test
    public void beachGame_redDisqualificationCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), null, "",
                                                 Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                 System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.RED_DISQUALIFICATION, 1);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(2, game.getSets(TeamType.GUEST));
        assertTrue(game.isMatchCompleted());

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getAllSanctions(TeamType.HOME).get(0).getCard());
    }

    @Test
    public void indoorGame_yellowCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.startMatch();

        fillCourt((IClassicTeam) game);

        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, 3);

        assertTrue(game.hasSanctions(TeamType.HOME, 3));
        assertFalse(game.hasSanctions(TeamType.HOME, 1));
        assertEquals(SanctionType.YELLOW, game.getAllSanctions(TeamType.HOME).get(0).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 3).contains(SanctionType.RED_DISQUALIFICATION));

        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 5).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 5).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 5).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 5).contains(SanctionType.RED_DISQUALIFICATION));
    }

    @Test
    public void indoorGame_redCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.startMatch();

        fillCourt((IClassicTeam) game);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.RED, 1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertFalse(game.hasSanctions(TeamType.HOME, 6));
        assertEquals(SanctionType.RED, game.getAllSanctions(TeamType.HOME).get(0).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.YELLOW));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 1).contains(SanctionType.RED_DISQUALIFICATION));

        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 4).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 4).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 4).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 4).contains(SanctionType.RED_DISQUALIFICATION));

        // coach
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.GUEST, SanctionType.RED, SanctionDto.COACH);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(1, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, SanctionDto.COACH));
        assertFalse(game.hasSanctions(TeamType.GUEST, 3));
        assertEquals(SanctionType.RED, game.getAllSanctions(TeamType.HOME).get(0).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.YELLOW));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_DISQUALIFICATION));

        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.RED_DISQUALIFICATION));
    }

    @Test
    public void indoorGame_redExpulsionCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        IClassicTeam indoorTeam = (IClassicTeam) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeam.addLibero(TeamType.GUEST, 7);

        game.startMatch();

        fillCourt(indoorTeam);

        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.RED_EXPULSION, 6);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.HOME, 6));
        assertEquals(SanctionType.RED_EXPULSION, game.getAllSanctions(TeamType.HOME).get(0).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.YELLOW));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.RED));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, 6).contains(SanctionType.RED_DISQUALIFICATION));

        // coach
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, SanctionDto.COACH);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, SanctionDto.COACH));
        assertEquals(SanctionType.RED_EXPULSION, game.getAllSanctions(TeamType.GUEST).get(0).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.YELLOW));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, SanctionDto.COACH).contains(SanctionType.RED_DISQUALIFICATION));

        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 4).contains(SanctionType.RED_DISQUALIFICATION));

        fillCourt(indoorTeam);

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 1);
        indoorTeam.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_1, ActionOriginType.USER);

        assertTrue(game.hasSanctions(TeamType.GUEST, 1));
        assertEquals(SanctionType.RED_EXPULSION, game.getAllSanctions(TeamType.GUEST).get(1).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 1).contains(SanctionType.YELLOW));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 1).contains(SanctionType.RED));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 1).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 1).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 5);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, 5));
        assertEquals(SanctionType.RED_EXPULSION, game.getAllSanctions(TeamType.GUEST).get(2).getCard());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 5).contains(SanctionType.YELLOW));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 5).contains(SanctionType.RED));
        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 5).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 5).contains(SanctionType.RED_DISQUALIFICATION));
    }

    @Test
    public void indoorGame_redDisqualificationCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), null, "",
                                                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                  System.currentTimeMillis(), Rules.officialIndoorRules());
        IClassicTeam indoorTeam = (IClassicTeam) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeam.addLibero(TeamType.GUEST, 7);

        game.startMatch();

        fillCourt(indoorTeam);

        // coach
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, SanctionDto.COACH).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, SanctionDto.COACH).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, SanctionDto.COACH).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, SanctionDto.COACH).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.HOME, SanctionType.RED_DISQUALIFICATION, SanctionDto.COACH);

        assertTrue(game.hasSanctions(TeamType.HOME, SanctionDto.COACH));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getAllSanctions(TeamType.HOME).get(0).getCard());
        assertTrue(game.getPossibleMisconductSanctions(TeamType.HOME, SanctionDto.COACH).isEmpty());

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertFalse(game.isMatchCompleted());

        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 3).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 3).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 3).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 3).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.GUEST, SanctionType.RED_DISQUALIFICATION, 3);
        indoorTeam.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_3, ActionOriginType.USER);

        assertTrue(game.hasSanctions(TeamType.GUEST, 3));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getAllSanctions(TeamType.GUEST).get(0).getCard());
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 3).isEmpty());

        assertFalse(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.YELLOW));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED_EXPULSION));
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).contains(SanctionType.RED_DISQUALIFICATION));

        game.giveSanction(TeamType.GUEST, SanctionType.RED_DISQUALIFICATION, 2);

        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertTrue(game.isMatchCompleted());

        assertTrue(game.hasSanctions(TeamType.GUEST, 2));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getAllSanctions(TeamType.GUEST).get(1).getCard());
        assertTrue(game.getPossibleMisconductSanctions(TeamType.GUEST, 2).isEmpty());
    }

    private void fillTeam(IGame game, TeamType teamType, int numberOfPlayers) {
        for (int number = 1; number <= numberOfPlayers; number++) {
            game.addPlayer(teamType, number);
        }
    }

    private void fillCourt(IClassicTeam indoorTeam) {
        for (int number = 1; number <= 6; number++) {
            indoorTeam.substitutePlayer(TeamType.HOME, number, PositionType.fromInt(number), ActionOriginType.USER);
            indoorTeam.substitutePlayer(TeamType.GUEST, number, PositionType.fromInt(number), ActionOriginType.USER);
        }
    }

}
