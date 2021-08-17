package com.tonkar.volleyballreferee;

import android.content.Context;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.api.model.ApiLeague;
import com.tonkar.volleyballreferee.engine.api.model.ApiTeam;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.game.GameFactory;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.Indoor4x4Game;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.scoresheet.ScoreSheetBuilder;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.service.StoredGamesService;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesManager;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesService;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsManager;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsService;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class Indoor4x4CompleteGame {

    private Context            mContext;
    private StoredGamesService mStoredGamesService;

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mStoredGamesService = new StoredGamesManager(mContext);
    }
    
    @Test
    public void playGame_complete() {
        ApiUserSummary user = PrefUtils.getUser(mContext);
        Indoor4x4Game indoor4x4Game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());

        defineTeamsAndLeague(indoor4x4Game);

        composeTeams(indoor4x4Game);
        playSet_complete(indoor4x4Game);

        composeTeams(indoor4x4Game);
        playSet_complete(indoor4x4Game);

        composeTeams(indoor4x4Game);
        playSet_complete(indoor4x4Game);

        IStoredGame storedGame = mStoredGamesService.getGame(indoor4x4Game.getId());
        ScoreSheetBuilder scoreSheetBuilder = new ScoreSheetBuilder(mContext, storedGame);
        scoreSheetBuilder.createScoreSheet();
    }

    private void defineTeamsAndLeague(Indoor4x4Game indoor4x4Game) {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(mContext);
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mContext);

        indoor4x4Game.setGender(GenderType.MIXED);

        ApiLeague league = storedLeaguesService.getLeague(GameType.INDOOR_4X4, "4x4");
        if (league == null) {
            indoor4x4Game.getLeague().setName("4x4");
            indoor4x4Game.getLeague().setDivision("Division 1");
        } else {
            indoor4x4Game.getLeague().setId(league.getId());
            indoor4x4Game.getLeague().setCreatedBy(league.getCreatedBy());
            indoor4x4Game.getLeague().setCreatedAt(league.getCreatedAt());
            indoor4x4Game.getLeague().setUpdatedAt(league.getUpdatedAt());
            indoor4x4Game.getLeague().setName(league.getName());
            indoor4x4Game.getLeague().setDivision("Division 1");
        }

        ApiTeam homeTeam = storedTeamsService.getTeam(GameType.INDOOR_4X4, "Home Team", GenderType.MIXED);
        ApiTeam guestTeam = storedTeamsService.getTeam(GameType.INDOOR_4X4, "Guest Team", GenderType.MIXED);

        if (homeTeam == null) {
            indoor4x4Game.setTeamName(TeamType.HOME, "Home Team");
            indoor4x4Game.setTeamColor(TeamType.HOME, Color.parseColor("#2980b9"));

            for (int index = 1; index <= 8; index++) {
                indoor4x4Game.addPlayer(TeamType.HOME, index);
            }

            indoor4x4Game.setCaptain(TeamType.HOME, 1);
        } else {
            storedTeamsService.copyTeam(homeTeam, indoor4x4Game, TeamType.HOME);
        }

        if (guestTeam == null) {
            indoor4x4Game.setTeamName(TeamType.GUEST, "Guest Team");
            indoor4x4Game.setTeamColor(TeamType.GUEST, Color.parseColor("#c2185b"));

            for (int index = 1; index <= 8; index++) {
                indoor4x4Game.addPlayer(TeamType.GUEST, index);
            }

            indoor4x4Game.setCaptain(TeamType.GUEST, 2);
        } else {
            storedTeamsService.copyTeam(guestTeam, indoor4x4Game, TeamType.GUEST);
        }

        storedTeamsService.createAndSaveTeamFrom(GameType.INDOOR_4X4, indoor4x4Game, TeamType.HOME);
        storedTeamsService.createAndSaveTeamFrom(GameType.INDOOR_4X4, indoor4x4Game, TeamType.GUEST);
        storedLeaguesService.createAndSaveLeagueFrom(indoor4x4Game.getLeague());

        indoor4x4Game.startMatch();

        mStoredGamesService.connectGameRecorder(indoor4x4Game);
    }

    private void composeTeams(Indoor4x4Game indoor4x4Game) {
        for (int index = 1; index <= 4; index++) {
            indoor4x4Game.substitutePlayer(TeamType.HOME, index, PositionType.fromInt(index), ActionOriginType.USER);
            indoor4x4Game.substitutePlayer(TeamType.GUEST, 5 - index, PositionType.fromInt(5 - index), ActionOriginType.USER);
        }

        indoor4x4Game.confirmStartingLineup(TeamType.HOME);
        indoor4x4Game.confirmStartingLineup(TeamType.GUEST);
    }

    private void playSet_complete(Indoor4x4Game indoor4x4Game) {
        indoor4x4Game.swapServiceAtStart();

        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.callTimeout(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.giveSanction(TeamType.GUEST, SanctionType.YELLOW, 2);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.callTimeout(TeamType.GUEST);
        indoor4x4Game.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_1, ActionOriginType.USER);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.substitutePlayer(TeamType.HOME, 6, PositionType.POSITION_3, ActionOriginType.USER);
        indoor4x4Game.substitutePlayer(TeamType.HOME, 8, PositionType.POSITION_1, ActionOriginType.USER);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
    }

}