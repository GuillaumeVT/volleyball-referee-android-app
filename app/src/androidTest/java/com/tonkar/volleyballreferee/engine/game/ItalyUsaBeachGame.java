package com.tonkar.volleyballreferee.engine.game;

import static org.junit.Assert.*;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.scoresheet.ScoreSheetBuilder;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.*;

import org.junit.*;
import org.junit.runner.RunWith;

import java.util.*;

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
        UserSummaryDto user = new UserSummaryDto(UUID.randomUUID().toString(), "user-pseudo");
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                                                          Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                          System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame, mContext, mStoredGamesService);

        playSet1_complete(beachGame);
        playSet2_complete(beachGame);

        IStoredGame storedGame = mStoredGamesService.getGame(beachGame.getId());
        ScoreSheetBuilder scoreSheetBuilder = new ScoreSheetBuilder(mContext, storedGame);
        scoreSheetBuilder.createScoreSheet();
    }

    @Test
    public void playGame_lastSetEnd() {
        playGame_lastSetEnd(mContext, mStoredGamesService);
    }

    public void playGame_lastSetEnd(Context context, StoredGamesService storedGamesService) {
        UserSummaryDto user = new UserSummaryDto(UUID.randomUUID().toString(), "user-pseudo");
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                                                          Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                          System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame, context, storedGamesService);

        playSet1_complete(beachGame);
        playSet2_lastSetEnd(beachGame);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int index = 0; index < 5; index++) {
            Log.i("VBR-Test", "playGame_matchPoint index #" + index);
            storedGamesService.saveCurrentGame(true);
            IGame game = storedGamesService.loadCurrentGame();
            assertNotEquals(null, game);
            assertEquals(beachGame, game);
        }
    }

    @Test
    public void playGame_technicalTimeout() {
        playGame_technicalTimeout(mContext, mStoredGamesService);
    }

    public void playGame_technicalTimeout(Context context, StoredGamesService storedGamesService) {
        UserSummaryDto user = new UserSummaryDto(UUID.randomUUID().toString(), "user-pseudo");
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                                                          Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                          System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame, context, storedGamesService);

        playSet1_technicalTimeout(beachGame);
    }

    private void defineTeamsAndLeague(BeachGame beachGame, Context context, StoredGamesService storedGamesService) {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(context);
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(context);

        beachGame.setGender(GenderType.GENTS);

        LeagueDto league = storedLeaguesService.getLeague(GameType.BEACH, "FIVB Beach Volleyball World Championship 2017");
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

        TeamDto teamUsa = storedTeamsService.getTeam(GameType.BEACH, "USA", GenderType.GENTS);
        TeamDto teamItaly = storedTeamsService.getTeam(GameType.BEACH, "ITALY", GenderType.GENTS);

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

        storedGamesService.connectGameRecorder(beachGame);
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
        playSet2_lastSetEnd(beachGame);
        beachGame.addPoint(TeamType.HOME);
        beachGame.addPoint(TeamType.GUEST);
        beachGame.addPoint(TeamType.HOME);
    }

    private void playSet2_lastSetEnd(BeachGame beachGame) {
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
    }
}
