package com.tonkar.volleyballreferee;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.game.GameFactory;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.team.IIndoorTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PenaltyCardsTest {

    @Test
    public void beachGame_yellowCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, 200);

        assertTrue(game.hasSanctions(TeamType.HOME, 200));
        assertEquals(SanctionType.YELLOW, game.getGivenSanctions(TeamType.HOME).get(0).getCard());
    }

    @Test
    public void beachGame_redCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        game.giveSanction(TeamType.HOME, SanctionType.RED, 1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertFalse(game.hasSanctions(TeamType.HOME, 5));
        assertEquals(SanctionType.RED, game.getGivenSanctions(TeamType.HOME).get(0).getCard());
    }

    @Test
    public void beachGame_redExpulsionCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 2);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, 2));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(0).getCard());
    }

    @Test
    public void beachGame_redDisqualificationCard() {
        IGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        game.giveSanction(TeamType.HOME, SanctionType.RED_DISQUALIFICATION, 1);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(2, game.getSets(TeamType.GUEST));
        assertTrue(game.isMatchCompleted());

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.HOME).get(0).getCard());
    }

    @Test
    public void indoorGame_yellowCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.startMatch();

        fillCourt((IIndoorTeam) game);

        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, 3);
        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, 100);

        assertTrue(game.hasSanctions(TeamType.HOME, 3));
        assertFalse(game.hasSanctions(TeamType.HOME, 1));
        assertEquals(SanctionType.YELLOW, game.getGivenSanctions(TeamType.HOME).get(0).getCard());
        assertEquals(SanctionType.YELLOW, game.getGivenSanctions(TeamType.HOME).get(1).getCard());
    }

    @Test
    public void indoorGame_redCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.startMatch();

        fillCourt((IIndoorTeam) game);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        game.giveSanction(TeamType.HOME, SanctionType.RED, 1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.HOME, 1));
        assertFalse(game.hasSanctions(TeamType.HOME, 6));
        assertEquals(SanctionType.RED, game.getGivenSanctions(TeamType.HOME).get(0).getCard());

        // coach
        game.giveSanction(TeamType.GUEST, SanctionType.RED, 100);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(1, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, 100));
        assertFalse(game.hasSanctions(TeamType.GUEST, 3));
        assertEquals(SanctionType.RED, game.getGivenSanctions(TeamType.HOME).get(0).getCard());
    }

    @Test
    public void indoorGame_redExpulsionCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());
        IIndoorTeam indoorTeam = (IIndoorTeam) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeam.addLibero(TeamType.GUEST, 7);

        game.startMatch();

        fillCourt(indoorTeam);

        game.giveSanction(TeamType.HOME, SanctionType.RED_EXPULSION, 6);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.HOME, 6));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.HOME).get(0).getCard());

        // coach
        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 100);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, 100));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(0).getCard());

        fillCourt(indoorTeam);

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 1);
        indoorTeam.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_1, ActionOriginType.USER);

        assertTrue(game.hasSanctions(TeamType.GUEST, 1));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(1).getCard());

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 5);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertTrue(game.hasSanctions(TeamType.GUEST, 5));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(2).getCard());
    }

    @Test
    public void indoorGame_redDisqualificationCard() {
        IGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), ApiUserSummary.VBR_USER_ID, "",
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());
        IIndoorTeam indoorTeam = (IIndoorTeam) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeam.addLibero(TeamType.GUEST, 7);

        game.startMatch();

        fillCourt(indoorTeam);

        // coach
        game.giveSanction(TeamType.HOME, SanctionType.RED_DISQUALIFICATION, 100);

        assertTrue(game.hasSanctions(TeamType.HOME, 100));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.HOME).get(0).getCard());

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertFalse(game.isMatchCompleted());

        game.giveSanction(TeamType.GUEST, SanctionType.RED_DISQUALIFICATION, 3);
        indoorTeam.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_3, ActionOriginType.USER);

        assertTrue(game.hasSanctions(TeamType.GUEST, 3));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.GUEST).get(0).getCard());

        game.giveSanction(TeamType.GUEST, SanctionType.RED_DISQUALIFICATION, 2);

        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertTrue(game.isMatchCompleted());

        assertTrue(game.hasSanctions(TeamType.GUEST, 2));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.GUEST).get(1).getCard());
    }

    private void fillTeam(IGame game, TeamType teamType, int numberOfPlayers) {
        for (int number = 1; number <= numberOfPlayers; number++) {
            game.addPlayer(teamType, number);
        }
    }

    private void fillCourt(IIndoorTeam indoorTeam) {
        for (int number = 1; number <= 6; number++) {
            indoorTeam.substitutePlayer(TeamType.HOME, number, PositionType.fromInt(number), ActionOriginType.USER);
            indoorTeam.substitutePlayer(TeamType.GUEST, number, PositionType.fromInt(number), ActionOriginType.USER);
        }
    }

}
