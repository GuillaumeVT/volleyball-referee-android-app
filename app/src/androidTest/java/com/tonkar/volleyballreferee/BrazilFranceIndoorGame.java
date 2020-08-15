package com.tonkar.volleyballreferee;

import android.graphics.Color;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.game.GameFactory;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.IndoorGame;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.engine.stored.ScoreSheetBuilder;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesService;
import com.tonkar.volleyballreferee.engine.stored.StoredLeaguesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredLeaguesService;
import com.tonkar.volleyballreferee.engine.stored.StoredTeamsManager;
import com.tonkar.volleyballreferee.engine.stored.StoredTeamsService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiLeague;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeam;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BrazilFranceIndoorGame {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private StoredGamesService mStoredGamesService;

    @Test
    public void playGame_complete() {
        ApiUserSummary user = PrefUtils.getUser(mActivityRule.getActivity());
        IndoorGame indoorGame = GameFactory.createIndoorGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());

        defineTeamsAndLeague(indoorGame);

        composeTeamsSet1(indoorGame);
        playSet1_complete(indoorGame);

        composeTeamsSet2(indoorGame);
        playSet2_complete(indoorGame);

        composeTeamsSet3(indoorGame);
        playSet3_complete(indoorGame);

        composeTeamsSet4(indoorGame);
        playSet4_complete(indoorGame);

        composeTeamsSet5(indoorGame);
        playSet5_complete(indoorGame);

        IStoredGame storedGame = mStoredGamesService.getGame(indoorGame.getId());
        ScoreSheetBuilder scoreSheetBuilder = new ScoreSheetBuilder(mActivityRule.getActivity(), storedGame);
        scoreSheetBuilder.createScoreSheet();
    }

    @Test
    public void playGame_lastSetEnd() {
        ApiUserSummary user = PrefUtils.getUser(mActivityRule.getActivity());
        IndoorGame indoorGame = GameFactory.createIndoorGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());

        defineTeamsAndLeague(indoorGame);

        composeTeamsSet1(indoorGame);
        playSet1_complete(indoorGame);

        composeTeamsSet2(indoorGame);
        playSet2_complete(indoorGame);

        composeTeamsSet3(indoorGame);
        playSet3_complete(indoorGame);

        composeTeamsSet4(indoorGame);
        playSet4_complete(indoorGame);

        composeTeamsSet5(indoorGame);
        playSet5_lastSetEnd(indoorGame);
    }

    @Test
    public void playGame_substitutions() {
        ApiUserSummary user = PrefUtils.getUser(mActivityRule.getActivity());
        IndoorGame indoorGame = GameFactory.createIndoorGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());

        defineTeamsAndLeague(indoorGame);

        composeTeamsSet1(indoorGame);
        playSet1_complete(indoorGame);

        composeTeamsSet2(indoorGame);
        playSet2_complete(indoorGame);

        composeTeamsSet3(indoorGame);
        playSet3_complete(indoorGame);

        composeTeamsSet4(indoorGame);
        playSet4_substitutions(indoorGame);
    }

    @Test
    public void playGame_io() {
        ApiUserSummary user = PrefUtils.getUser(mActivityRule.getActivity());
        IndoorGame indoorGame = GameFactory.createIndoorGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialIndoorRules());

        defineTeamsAndLeague(indoorGame);

        composeTeamsSet1(indoorGame);
        playSet1_complete(indoorGame);

        composeTeamsSet2(indoorGame);
        playSet2_complete(indoorGame);

        composeTeamsSet3(indoorGame);
        playSet3_complete(indoorGame);

        composeTeamsSet4(indoorGame);
        playSet4_substitutions(indoorGame);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int index = 0; index < 200; index++) {
            Log.i("VBR-Test", "playGame_io index #" + index);
            mStoredGamesService.saveCurrentGame(true);
            IGame game = mStoredGamesService.loadCurrentGame();
            assertNotEquals(null, game);
            assertEquals(indoorGame, game);
        }
    }

    private void defineTeamsAndLeague(IndoorGame indoorGame) {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(mActivityRule.getActivity());
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mActivityRule.getActivity());

        indoorGame.setGender(GenderType.GENTS);

        ApiLeague league = storedLeaguesService.getLeague(GameType.INDOOR, "FIVB Volleyball World League 2017");
        if (league == null) {
            indoorGame.getLeague().setName("FIVB Volleyball World League 2017");
            indoorGame.getLeague().setDivision("Final");
        } else {
            indoorGame.getLeague().setId(league.getId());
            indoorGame.getLeague().setCreatedBy(league.getCreatedBy());
            indoorGame.getLeague().setCreatedAt(league.getCreatedAt());
            indoorGame.getLeague().setUpdatedAt(league.getUpdatedAt());
            indoorGame.getLeague().setName(league.getName());
            indoorGame.getLeague().setDivision("Final");
        }

        ApiTeam teamBrazil = storedTeamsService.getTeam(GameType.INDOOR, "BRAZIL", GenderType.GENTS);
        ApiTeam teamFrance = storedTeamsService.getTeam(GameType.INDOOR, "FRANCE", GenderType.GENTS);

        if (teamBrazil == null) {
            indoorGame.setTeamName(TeamType.HOME, "BRAZIL");
            indoorGame.setTeamColor(TeamType.HOME, Color.parseColor("#f3bc07"));
            indoorGame.setLiberoColor(TeamType.HOME, Color.parseColor("#034694"));

            indoorGame.addPlayer(TeamType.HOME, 1);
            indoorGame.addPlayer(TeamType.HOME, 3);
            indoorGame.addPlayer(TeamType.HOME, 4);
            indoorGame.addPlayer(TeamType.HOME, 5);
            indoorGame.addPlayer(TeamType.HOME, 6);
            indoorGame.addPlayer(TeamType.HOME, 8);
            indoorGame.addPlayer(TeamType.HOME, 9);
            indoorGame.addPlayer(TeamType.HOME, 10);
            indoorGame.addPlayer(TeamType.HOME, 11);
            indoorGame.addPlayer(TeamType.HOME, 13);
            indoorGame.addPlayer(TeamType.HOME, 16);
            indoorGame.addPlayer(TeamType.HOME, 18);
            indoorGame.addPlayer(TeamType.HOME, 19);
            indoorGame.addPlayer(TeamType.HOME, 20);

            indoorGame.addLibero(TeamType.HOME, 6);
            indoorGame.addLibero(TeamType.HOME, 8);

            indoorGame.setCaptain(TeamType.HOME, 1);

            indoorGame.setPlayerName(TeamType.HOME, 1, "Bruno Mossa Rezende");
            indoorGame.setPlayerName(TeamType.HOME, 3, "Eder Carbonera");
            indoorGame.setPlayerName(TeamType.HOME, 4, "Wallace De Souza");
            indoorGame.setPlayerName(TeamType.HOME, 5, "Lucas Eduardo Loh");
            indoorGame.setPlayerName(TeamType.HOME, 6, "Tiago Brendle");
            indoorGame.setPlayerName(TeamType.HOME, 8, "Thales Hoss");
            indoorGame.setPlayerName(TeamType.HOME, 9, "Raphael Vieira De Oliveira");
            indoorGame.setPlayerName(TeamType.HOME, 10, "Otávio Henrique Rodrigues Pinto");
            indoorGame.setPlayerName(TeamType.HOME, 11, "Rodrigo Leao");
            indoorGame.setPlayerName(TeamType.HOME, 16, "Lucas Saatkamp");
            indoorGame.setPlayerName(TeamType.HOME, 18, "Ricardo Lucarelli Souza");
            indoorGame.setPlayerName(TeamType.HOME, 19, "Mauricio Borges Almeida Silva");
            indoorGame.setPlayerName(TeamType.HOME, 20, "Renan Buiatti");

            indoorGame.setCoachName(TeamType.HOME, "Renan Dal Zotto");
        } else {
            storedTeamsService.copyTeam(teamBrazil, indoorGame, TeamType.HOME);
        }

        if (teamFrance == null) {
            indoorGame.setTeamName(TeamType.GUEST, "FRANCE");
            indoorGame.setTeamColor(TeamType.GUEST, Color.parseColor("#034694"));
            indoorGame.setLiberoColor(TeamType.GUEST, Color.parseColor("#bc0019"));

            indoorGame.addPlayer(TeamType.GUEST, 2);
            indoorGame.addPlayer(TeamType.GUEST, 5);
            indoorGame.addPlayer(TeamType.GUEST, 6);
            indoorGame.addPlayer(TeamType.GUEST, 8);
            indoorGame.addPlayer(TeamType.GUEST, 9);
            indoorGame.addPlayer(TeamType.GUEST, 10);
            indoorGame.addPlayer(TeamType.GUEST, 11);
            indoorGame.addPlayer(TeamType.GUEST, 12);
            indoorGame.addPlayer(TeamType.GUEST, 14);
            indoorGame.addPlayer(TeamType.GUEST, 16);
            indoorGame.addPlayer(TeamType.GUEST, 17);
            indoorGame.addPlayer(TeamType.GUEST, 18);
            indoorGame.addPlayer(TeamType.GUEST, 20);
            indoorGame.addPlayer(TeamType.GUEST, 21);

            indoorGame.addLibero(TeamType.GUEST, 2);
            indoorGame.addLibero(TeamType.GUEST, 20);

            indoorGame.setCaptain(TeamType.GUEST, 6);

            indoorGame.setPlayerName(TeamType.GUEST, 2, "Jenia Grebennikov");
            indoorGame.setPlayerName(TeamType.GUEST, 5, "Trevor Clevenot");
            indoorGame.setPlayerName(TeamType.GUEST, 6, "Benjamin Toniutti");
            indoorGame.setPlayerName(TeamType.GUEST, 8, "Julien Lyneel");
            indoorGame.setPlayerName(TeamType.GUEST, 9, "Earvin Ngapeth");
            indoorGame.setPlayerName(TeamType.GUEST, 10, "Kevin Le Roux");
            indoorGame.setPlayerName(TeamType.GUEST, 11, "Antoine Brizard");
            indoorGame.setPlayerName(TeamType.GUEST, 12, "Stephen Boyer");
            indoorGame.setPlayerName(TeamType.GUEST, 14, "Nicolas Le Goff");
            indoorGame.setPlayerName(TeamType.GUEST, 16, "Daryl Bultor");
            indoorGame.setPlayerName(TeamType.GUEST, 17, "Guillaume Quesque");
            indoorGame.setPlayerName(TeamType.GUEST, 18, "Thibault Rossard");
            indoorGame.setPlayerName(TeamType.GUEST, 20, "Nicolas Rossard");
            indoorGame.setPlayerName(TeamType.GUEST, 21, "Barthélémy Chinenyeze");

            indoorGame.setCoachName(TeamType.GUEST, "Laurent Tillie");
        } else {
            storedTeamsService.copyTeam(teamFrance, indoorGame, TeamType.GUEST);
        }

        storedTeamsService.createAndSaveTeamFrom(GameType.INDOOR, indoorGame, TeamType.HOME);
        storedTeamsService.createAndSaveTeamFrom(GameType.INDOOR, indoorGame, TeamType.GUEST);
        storedLeaguesService.createAndSaveLeagueFrom(indoorGame.getLeague());

        indoorGame.setReferee1Name("Referee 1");
        indoorGame.setReferee2Name("Referee 2");
        indoorGame.setScorerName("Scorer");

        indoorGame.startMatch();

        mStoredGamesService = new StoredGamesManager(mActivityRule.getActivity());
        mStoredGamesService.connectGameRecorder(indoorGame);
    }

    private void composeTeamsSet1(IndoorGame indoorGame) {
        indoorGame.substitutePlayer(TeamType.HOME, 18, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 13, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 19, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 16, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 4, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.HOME);

        indoorGame.substitutePlayer(TeamType.GUEST, 6, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 9, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 10, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 12, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 21, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.GUEST);

        indoorGame.substitutePlayer(TeamType.HOME, 8, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_6, ActionOriginType.USER);
    }

    private void playSet1_complete(IndoorGame indoorGame) {
        indoorGame.swapServiceAtStart();

        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.callTimeout(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
    }

    private void composeTeamsSet2(IndoorGame indoorGame) {
        indoorGame.substitutePlayer(TeamType.HOME, 19, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 16, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 4, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 18, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 13, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.HOME);

        indoorGame.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 21, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 6, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 9, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 10, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 12, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.GUEST);

        indoorGame.substitutePlayer(TeamType.HOME, 8, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_5, ActionOriginType.USER);
    }

    private void playSet2_complete(IndoorGame indoorGame) {
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.substitutePlayer(TeamType.HOME, 20, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 9, PositionType.POSITION_6, ActionOriginType.USER);
        indoorGame.setGameCaptain(TeamType.HOME, 20);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
    }

    private void composeTeamsSet3(IndoorGame indoorGame) {
        indoorGame.substitutePlayer(TeamType.HOME, 4, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 18, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 13, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 19, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 16, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.HOME);

        indoorGame.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 21, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 6, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 9, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 10, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 12, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.GUEST);

        indoorGame.substitutePlayer(TeamType.HOME, 8, PositionType.POSITION_6, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_5, ActionOriginType.USER);
    }

    private void playSet3_complete(IndoorGame indoorGame) {
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.substitutePlayer(TeamType.GUEST, 11, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.setGameCaptain(TeamType.GUEST, 8);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
    }

    private void composeTeamsSet4(IndoorGame indoorGame) {
        indoorGame.substitutePlayer(TeamType.HOME, 19, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 16, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 4, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 18, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.HOME);

        indoorGame.substitutePlayer(TeamType.GUEST, 12, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 21, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 6, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 9, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 10, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.GUEST);

        indoorGame.substitutePlayer(TeamType.HOME, 8, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_6, ActionOriginType.USER);
    }

    private void playSet4_complete(IndoorGame indoorGame) {
        playSet4_substitutions(indoorGame);

        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
    }

    private void playSet4_substitutions(IndoorGame indoorGame) {
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.callTimeout(TeamType.GUEST);
        indoorGame.substitutePlayer(TeamType.GUEST, 8, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.callTimeout(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.substitutePlayer(TeamType.HOME, 9, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 20, PositionType.POSITION_6, ActionOriginType.USER);
        indoorGame.setGameCaptain(TeamType.HOME, 9);
        indoorGame.substitutePlayer(TeamType.GUEST, 11, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.setGameCaptain(TeamType.GUEST, 9);
        indoorGame.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_2, ActionOriginType.USER);
    }

    private void composeTeamsSet5(IndoorGame indoorGame) {
        indoorGame.swapTeams(ActionOriginType.USER);

        indoorGame.substitutePlayer(TeamType.HOME, 19, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 16, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 4, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 18, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.HOME);

        indoorGame.substitutePlayer(TeamType.GUEST, 6, PositionType.POSITION_1, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 9, PositionType.POSITION_2, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 10, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 12, PositionType.POSITION_4, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 21, PositionType.POSITION_6, ActionOriginType.USER);

        indoorGame.confirmStartingLineup(TeamType.GUEST);

        indoorGame.substitutePlayer(TeamType.HOME, 8, PositionType.POSITION_5, ActionOriginType.USER);
        indoorGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_6, ActionOriginType.USER);
    }

    private void playSet5_complete(IndoorGame indoorGame) {
        playSet5_lastSetEnd(indoorGame);

        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
    }

    private void playSet5_lastSetEnd(IndoorGame indoorGame) {
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.callTimeout(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.callTimeout(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.substitutePlayer(TeamType.HOME, 13, PositionType.POSITION_3, ActionOriginType.USER);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.callTimeout(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.GUEST);
        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.GUEST);
    }
}