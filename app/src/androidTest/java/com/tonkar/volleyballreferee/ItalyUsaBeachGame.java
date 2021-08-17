package com.tonkar.volleyballreferee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.api.model.ApiLeague;
import com.tonkar.volleyballreferee.engine.api.model.ApiTeam;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.game.BeachGame;
import com.tonkar.volleyballreferee.engine.game.GameFactory;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.IGame;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ItalyUsaBeachGame {

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
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame);

        playSet1_complete(beachGame);
        playSet2_complete(beachGame);

        IStoredGame storedGame = mStoredGamesService.getGame(beachGame.getId());
        ScoreSheetBuilder scoreSheetBuilder = new ScoreSheetBuilder(mContext, storedGame);
        scoreSheetBuilder.createScoreSheet();
    }

    @Test
    public void playGame_matchPoint() {
        ApiUserSummary user = PrefUtils.getUser(mContext);
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame);

        playSet1_complete(beachGame);
        playSet2_matchPoint(beachGame);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int index = 0; index < 5; index++) {
            Log.i("VBR-Test", "playGame_matchPoint index #" + index);
            mStoredGamesService.saveCurrentGame(true);
            IGame game = mStoredGamesService.loadCurrentGame();
            assertNotEquals(null, game);
            assertEquals(beachGame, game);
        }
    }

    @Test
    public void playGame_technicalTimeout() {
        ApiUserSummary user = PrefUtils.getUser(mContext);
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame);

        playSet1_technicalTimeout(beachGame);
    }

    private void defineTeamsAndLeague(BeachGame beachGame) {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(mContext);
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mContext);

        beachGame.setGender(GenderType.GENTS);

        ApiLeague league = storedLeaguesService.getLeague(GameType.BEACH, "FIVB Beach Volleyball World Championship 2017");
        if (league == null) {
            beachGame.getLeague().setName("FIVB Beach Volleyball World Championship 2017");
            beachGame.getLeague().setDivision("Final");
        } else {
            beachGame.getLeague().setId(league.getId());
            beachGame.getLeague().setCreatedBy(league.getCreatedBy());
            beachGame.getLeague().setCreatedAt(league.getCreatedAt());
            beachGame.getLeague().setUpdatedAt(league.getUpdatedAt());
            beachGame.getLeague().setName(league.getName());
            beachGame.getLeague().setDivision("Final");
        }

        ApiTeam teamUsa = storedTeamsService.getTeam(GameType.BEACH, "USA", GenderType.GENTS);
        ApiTeam teamItaly = storedTeamsService.getTeam(GameType.BEACH, "ITALY", GenderType.GENTS);

        if (teamUsa == null) {
            beachGame.setTeamName(TeamType.HOME, "USA");
            beachGame.setTeamColor(TeamType.HOME, Color.parseColor("#bc0019"));
            beachGame.setPlayerName(TeamType.HOME, 1, "Leon");
            beachGame.setPlayerName(TeamType.HOME, 2, "Gustave");
        } else {
            storedTeamsService.copyTeam(teamUsa, beachGame, TeamType.HOME);
        }

        if (teamItaly == null) {
            beachGame.setTeamName(TeamType.GUEST, "ITALY");
            beachGame.setTeamColor(TeamType.GUEST, Color.parseColor("#2980b9"));
            beachGame.setPlayerName(TeamType.GUEST, 1, "Robert");
            beachGame.setPlayerName(TeamType.GUEST, 2, "Gerard");
        } else {
            storedTeamsService.copyTeam(teamItaly, beachGame, TeamType.GUEST);
        }

        storedTeamsService.createAndSaveTeamFrom(GameType.BEACH, beachGame, TeamType.HOME);
        storedTeamsService.createAndSaveTeamFrom(GameType.BEACH, beachGame, TeamType.GUEST);
        storedLeaguesService.createAndSaveLeagueFrom(beachGame.getLeague());

        beachGame.startMatch();

        mStoredGamesService.connectGameRecorder(beachGame);
    }

    private void playSet1_complete(BeachGame beachGame) {
        playSet1_technicalTimeout(beachGame);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.callTimeout(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
    }

    private void playSet1_technicalTimeout(BeachGame beachGame) {
        beachGame.swapServiceAtStart();
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.callTimeout(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
    }

    private void playSet2_complete(BeachGame beachGame) {
        playSet2_matchPoint(beachGame);
        beachGame.addPoint(TeamType.HOME);
    }

    private void playSet2_matchPoint(BeachGame beachGame) {
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.callTimeout(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.callTimeout(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
    }
}
