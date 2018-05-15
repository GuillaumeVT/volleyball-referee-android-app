package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class PenaltyCardsTest {

    @Test
    public void beachGame_yellowCard() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, -1);

        assertEquals(true, game.hasSanctions(TeamType.HOME, -1));
        assertEquals(SanctionType.YELLOW, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());
    }

    @Test
    public void beachGame_redCard() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        game.giveSanction(TeamType.HOME, SanctionType.RED, -1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertEquals(true, game.hasSanctions(TeamType.HOME, -1));
        assertEquals(false, game.hasSanctions(TeamType.HOME, 5));
        assertEquals(SanctionType.RED, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());
    }

    @Test
    public void beachGame_redExpulsionCard() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, -1);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasSanctions(TeamType.GUEST, -1));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(0).getSanctionType());
    }

    @Test
    public void beachGame_redDisqualificationCard() {
        GameService game = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());
        game.startMatch();

        game.giveSanction(TeamType.HOME, SanctionType.RED_DISQUALIFICATION, -1);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(2, game.getSets(TeamType.GUEST));
        assertEquals(true, game.isMatchCompleted());

        assertEquals(true, game.hasSanctions(TeamType.HOME, -1));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());
    }

    @Test
    public void indoorGame_yellowCard() {
        GameService game = GameFactory.createIndoorGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialIndoorRules());

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.startMatch();

        fillCourt((IndoorTeamService) game);

        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, 3);
        game.giveSanction(TeamType.HOME, SanctionType.YELLOW, 0);

        assertEquals(true, game.hasSanctions(TeamType.HOME, 3));
        assertEquals(false, game.hasSanctions(TeamType.HOME, 1));
        assertEquals(SanctionType.YELLOW, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());
        assertEquals(SanctionType.YELLOW, game.getGivenSanctions(TeamType.HOME).get(1).getSanctionType());
    }

    @Test
    public void indoorGame_redCard() {
        GameService game = GameFactory.createIndoorGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialIndoorRules());

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.startMatch();

        fillCourt((IndoorTeamService) game);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        game.giveSanction(TeamType.HOME, SanctionType.RED, 1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertEquals(true, game.hasSanctions(TeamType.HOME, 1));
        assertEquals(false, game.hasSanctions(TeamType.HOME, 6));
        assertEquals(SanctionType.RED, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());

        // coach
        game.giveSanction(TeamType.GUEST, SanctionType.RED, 0);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(1, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertEquals(true, game.hasSanctions(TeamType.GUEST, 0));
        assertEquals(false, game.hasSanctions(TeamType.GUEST, 3));
        assertEquals(SanctionType.RED, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());
    }

    @Test
    public void indoorGame_redExpulsionCard() {
        GameService game = GameFactory.createIndoorGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialIndoorRules());
        IndoorTeamService indoorTeamService = (IndoorTeamService) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeamService.addLibero(TeamType.GUEST, 7);

        game.startMatch();

        fillCourt(indoorTeamService);

        game.giveSanction(TeamType.HOME, SanctionType.RED_EXPULSION, 6);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasSanctions(TeamType.HOME, 6));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());

        // coach
        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 0);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasSanctions(TeamType.GUEST, 0));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(0).getSanctionType());

        fillCourt(indoorTeamService);

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 1);
        indoorTeamService.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_1, ActionOriginType.USER);

        assertEquals(true, game.hasSanctions(TeamType.GUEST, 1));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(1).getSanctionType());

        game.giveSanction(TeamType.GUEST, SanctionType.RED_EXPULSION, 5);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasSanctions(TeamType.GUEST, 5));
        assertEquals(SanctionType.RED_EXPULSION, game.getGivenSanctions(TeamType.GUEST).get(2).getSanctionType());
    }

    @Test
    public void indoorGame_redDisqualificationCard() {
        GameService game = GameFactory.createIndoorGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialIndoorRules());
        IndoorTeamService indoorTeamService = (IndoorTeamService) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeamService.addLibero(TeamType.GUEST, 7);

        game.startMatch();

        fillCourt(indoorTeamService);

        // coach
        game.giveSanction(TeamType.HOME, SanctionType.RED_DISQUALIFICATION, 0);

        assertEquals(true, game.hasSanctions(TeamType.HOME, 0));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.HOME).get(0).getSanctionType());

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(false, game.isMatchCompleted());

        game.giveSanction(TeamType.GUEST, SanctionType.RED_DISQUALIFICATION, 3);
        indoorTeamService.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_3, ActionOriginType.USER);

        assertEquals(true, game.hasSanctions(TeamType.GUEST, 3));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.GUEST).get(0).getSanctionType());

        game.giveSanction(TeamType.GUEST, SanctionType.RED_DISQUALIFICATION, 2);

        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(true, game.isMatchCompleted());

        assertEquals(true, game.hasSanctions(TeamType.GUEST, 2));
        assertEquals(SanctionType.RED_DISQUALIFICATION, game.getGivenSanctions(TeamType.GUEST).get(1).getSanctionType());
    }

    private void fillTeam(GameService game, TeamType teamType, int numberOfPlayers) {
        for (int number = 1; number <= numberOfPlayers; number++) {
            game.addPlayer(teamType, number);
        }
    }

    private void fillCourt(IndoorTeamService indoorTeamService) {
        for (int number = 1; number <= 6; number++) {
            indoorTeamService.substitutePlayer(TeamType.HOME, number, PositionType.fromInt(number), ActionOriginType.USER);
            indoorTeamService.substitutePlayer(TeamType.GUEST, number, PositionType.fromInt(number), ActionOriginType.USER);
        }
    }

}
