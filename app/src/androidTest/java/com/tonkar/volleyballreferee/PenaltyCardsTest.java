package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.card.PenaltyCardType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class PenaltyCardsTest {

    @Test
    public void beachGame_yellowCard() {
        GameService game = GameFactory.createBeachGame();
        game.initTeams();

        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.YELLOW, -1);

        assertEquals(true, game.hasPenaltyCards(TeamType.HOME, -1));
        assertEquals(PenaltyCardType.YELLOW, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());
    }

    @Test
    public void beachGame_redCard() {
        GameService game = GameFactory.createBeachGame();
        game.initTeams();

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.RED, -1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertEquals(true, game.hasPenaltyCards(TeamType.HOME, -1));
        assertEquals(false, game.hasPenaltyCards(TeamType.HOME, 5));
        assertEquals(PenaltyCardType.RED, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());
    }

    @Test
    public void beachGame_redExpulsionCard() {
        GameService game = GameFactory.createBeachGame();
        game.initTeams();

        game.givePenaltyCard(TeamType.GUEST, PenaltyCardType.RED_EXPULSION, -1);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasPenaltyCards(TeamType.GUEST, -1));
        assertEquals(PenaltyCardType.RED_EXPULSION, game.getGivenPenaltyCards(TeamType.GUEST).get(0).getPenaltyCardType());
    }

    @Test
    public void beachGame_redDisqualificationCard() {
        GameService game = GameFactory.createBeachGame();
        game.initTeams();

        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.RED_DISQUALIFICATION, -1);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(2, game.getSets(TeamType.GUEST));
        assertEquals(true, game.isMatchCompleted());

        assertEquals(true, game.hasPenaltyCards(TeamType.HOME, -1));
        assertEquals(PenaltyCardType.RED_DISQUALIFICATION, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());
    }

    @Test
    public void indoorGame_yellowCard() {
        GameService game = GameFactory.createIndoorGame();

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.initTeams();

        fillCourt((IndoorTeamService) game);

        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.YELLOW, 3);
        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.YELLOW, 0);

        assertEquals(true, game.hasPenaltyCards(TeamType.HOME, 3));
        assertEquals(false, game.hasPenaltyCards(TeamType.HOME, 1));
        assertEquals(PenaltyCardType.YELLOW, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());
        assertEquals(PenaltyCardType.YELLOW, game.getGivenPenaltyCards(TeamType.HOME).get(1).getPenaltyCardType());
    }

    @Test
    public void indoorGame_redCard() {
        GameService game = GameFactory.createIndoorGame();

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 7);

        game.initTeams();

        fillCourt((IndoorTeamService) game);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(0, game.getPoints(TeamType.GUEST));

        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.RED, 1);

        assertEquals(TeamType.GUEST, game.getServingTeam());
        assertEquals(0, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertEquals(true, game.hasPenaltyCards(TeamType.HOME, 1));
        assertEquals(false, game.hasPenaltyCards(TeamType.HOME, 6));
        assertEquals(PenaltyCardType.RED, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());

        // coach
        game.givePenaltyCard(TeamType.GUEST, PenaltyCardType.RED, 0);

        assertEquals(TeamType.HOME, game.getServingTeam());
        assertEquals(1, game.getPoints(TeamType.HOME));
        assertEquals(1, game.getPoints(TeamType.GUEST));

        assertEquals(true, game.hasPenaltyCards(TeamType.GUEST, 0));
        assertEquals(false, game.hasPenaltyCards(TeamType.GUEST, 3));
        assertEquals(PenaltyCardType.RED, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());
    }

    @Test
    public void indoorGame_redExpulsionCard() {
        GameService game = GameFactory.createIndoorGame();
        IndoorTeamService indoorTeamService = (IndoorTeamService) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeamService.addLibero(TeamType.GUEST, 7);

        game.initTeams();

        fillCourt(indoorTeamService);

        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.RED_EXPULSION, 6);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasPenaltyCards(TeamType.HOME, 6));
        assertEquals(PenaltyCardType.RED_EXPULSION, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());

        // coach
        game.givePenaltyCard(TeamType.GUEST, PenaltyCardType.RED_EXPULSION, 0);

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasPenaltyCards(TeamType.GUEST, 0));
        assertEquals(PenaltyCardType.RED_EXPULSION, game.getGivenPenaltyCards(TeamType.GUEST).get(0).getPenaltyCardType());

        fillCourt(indoorTeamService);

        game.givePenaltyCard(TeamType.GUEST, PenaltyCardType.RED_EXPULSION, 1);
        indoorTeamService.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_1, ActionOriginType.USER);

        assertEquals(true, game.hasPenaltyCards(TeamType.GUEST, 1));
        assertEquals(PenaltyCardType.RED_EXPULSION, game.getGivenPenaltyCards(TeamType.GUEST).get(1).getPenaltyCardType());

        game.givePenaltyCard(TeamType.GUEST, PenaltyCardType.RED_EXPULSION, 5);

        assertEquals(1, game.getSets(TeamType.HOME));
        assertEquals(1, game.getSets(TeamType.GUEST));

        assertEquals(true, game.hasPenaltyCards(TeamType.GUEST, 5));
        assertEquals(PenaltyCardType.RED_EXPULSION, game.getGivenPenaltyCards(TeamType.GUEST).get(2).getPenaltyCardType());
    }

    @Test
    public void indoorGame_redDisqualificationCard() {
        GameService game = GameFactory.createIndoorGame();
        IndoorTeamService indoorTeamService = (IndoorTeamService) game;

        fillTeam(game, TeamType.HOME, 6);
        fillTeam(game, TeamType.GUEST, 8);
        indoorTeamService.addLibero(TeamType.GUEST, 7);

        game.initTeams();

        fillCourt(indoorTeamService);

        // coach
        game.givePenaltyCard(TeamType.HOME, PenaltyCardType.RED_DISQUALIFICATION, 0);

        assertEquals(true, game.hasPenaltyCards(TeamType.HOME, 0));
        assertEquals(PenaltyCardType.RED_DISQUALIFICATION, game.getGivenPenaltyCards(TeamType.HOME).get(0).getPenaltyCardType());

        assertEquals(0, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(false, game.isMatchCompleted());

        game.givePenaltyCard(TeamType.GUEST, PenaltyCardType.RED_DISQUALIFICATION, 3);
        indoorTeamService.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_3, ActionOriginType.USER);

        assertEquals(true, game.hasPenaltyCards(TeamType.GUEST, 3));
        assertEquals(PenaltyCardType.RED_DISQUALIFICATION, game.getGivenPenaltyCards(TeamType.GUEST).get(0).getPenaltyCardType());

        game.givePenaltyCard(TeamType.GUEST, PenaltyCardType.RED_DISQUALIFICATION, 2);

        assertEquals(3, game.getSets(TeamType.HOME));
        assertEquals(0, game.getSets(TeamType.GUEST));
        assertEquals(true, game.isMatchCompleted());

        assertEquals(true, game.hasPenaltyCards(TeamType.GUEST, 2));
        assertEquals(PenaltyCardType.RED_DISQUALIFICATION, game.getGivenPenaltyCards(TeamType.GUEST).get(1).getPenaltyCardType());
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
