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
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.*;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RussiaUsaSnowGame {

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
        SnowGame snowGame = GameFactory.createSnowGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                                                       Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                       System.currentTimeMillis(), Rules.officialSnowRules());

        defineTeamsAndLeague(snowGame);

        playSet1_complete(snowGame);
        playSet2_complete(snowGame);
        playSet3_complete(snowGame);

        IStoredGame storedGame = mStoredGamesService.getGame(snowGame.getId());
        ScoreSheetBuilder scoreSheetBuilder = new ScoreSheetBuilder(mContext, storedGame);
        scoreSheetBuilder.createScoreSheet();
    }

    @Test
    public void playGame_matchPoint() {
        UserSummaryDto user = new UserSummaryDto(UUID.randomUUID().toString(), "user-pseudo");
        SnowGame snowGame = GameFactory.createSnowGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                                                       Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                       System.currentTimeMillis(), Rules.officialSnowRules());

        defineTeamsAndLeague(snowGame);

        playSet1_complete(snowGame);
        playSet2_complete(snowGame);
        playSet3_matchPoint(snowGame);

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
            assertEquals(snowGame, game);
        }
    }

    private void defineTeamsAndLeague(SnowGame snowGame) {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(mContext);
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mContext);

        snowGame.setGender(GenderType.GENTS);

        LeagueDto league = storedLeaguesService.getLeague(GameType.SNOW, "FIVB Snow Volleyball Kronplatz 2019");
        if (league == null) {
            snowGame.getLeague().setName("FIVB Snow Volleyball Kronplatz 2019");
            snowGame.getLeague().setDivision("Final");
        } else {
            snowGame.getLeague().setId(league.getId());
            snowGame.getLeague().setCreatedBy(league.getCreatedBy());
            snowGame.getLeague().setCreatedAt(league.getCreatedAt());
            snowGame.getLeague().setUpdatedAt(league.getUpdatedAt());
            snowGame.getLeague().setName(league.getName());
            snowGame.getLeague().setDivision("Final");
        }

        TeamDto teamRussia = storedTeamsService.getTeam(GameType.SNOW, "RUSSIA 1", GenderType.GENTS);
        TeamDto teamUsa = storedTeamsService.getTeam(GameType.SNOW, "USA 2", GenderType.GENTS);

        if (teamRussia == null) {
            snowGame.setTeamName(TeamType.HOME, "RUSSIA 1");
            snowGame.setTeamColor(TeamType.HOME, Color.parseColor("#bc0019"));
        } else {
            storedTeamsService.copyTeam(teamRussia, snowGame, TeamType.HOME);
        }

        if (teamUsa == null) {
            snowGame.setTeamName(TeamType.GUEST, "USA 2");
            snowGame.setTeamColor(TeamType.GUEST, Color.parseColor("#2980b9"));
        } else {
            storedTeamsService.copyTeam(teamUsa, snowGame, TeamType.GUEST);
        }

        storedTeamsService.createAndSaveTeamFrom(GameType.SNOW, snowGame, TeamType.HOME);
        storedTeamsService.createAndSaveTeamFrom(GameType.SNOW, snowGame, TeamType.GUEST);
        storedLeaguesService.createAndSaveLeagueFrom(snowGame.getLeague());

        snowGame.startMatch();

        mStoredGamesService.connectGameRecorder(snowGame);
    }

    private void playSet1_complete(SnowGame snowGame) {
        snowGame.swapServiceAtStart();

        snowGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_1, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.HOME, 2, PositionType.POSITION_2, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_3, ActionOriginType.USER);
        snowGame.confirmStartingLineup(TeamType.HOME);

        snowGame.substitutePlayer(TeamType.GUEST, 1, PositionType.POSITION_1, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_2, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.GUEST, 3, PositionType.POSITION_3, ActionOriginType.USER);
        snowGame.confirmStartingLineup(TeamType.GUEST);

        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
    }

    private void playSet2_complete(SnowGame snowGame) {
        snowGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_1, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.HOME, 2, PositionType.POSITION_2, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_3, ActionOriginType.USER);
        snowGame.confirmStartingLineup(TeamType.HOME);

        snowGame.substitutePlayer(TeamType.GUEST, 1, PositionType.POSITION_1, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_2, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.GUEST, 3, PositionType.POSITION_3, ActionOriginType.USER);
        snowGame.confirmStartingLineup(TeamType.GUEST);

        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
    }

    private void playSet3_matchPoint(SnowGame snowGame) {
        snowGame.substitutePlayer(TeamType.HOME, 1, PositionType.POSITION_1, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.HOME, 2, PositionType.POSITION_2, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.HOME, 3, PositionType.POSITION_3, ActionOriginType.USER);
        snowGame.confirmStartingLineup(TeamType.HOME);

        snowGame.substitutePlayer(TeamType.GUEST, 2, PositionType.POSITION_1, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.GUEST, 3, PositionType.POSITION_2, ActionOriginType.USER);
        snowGame.substitutePlayer(TeamType.GUEST, 1, PositionType.POSITION_3, ActionOriginType.USER);
        snowGame.confirmStartingLineup(TeamType.GUEST);

        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.HOME);
        snowGame.addPoint(TeamType.GUEST);
        snowGame.addPoint(TeamType.GUEST);
    }

    private void playSet3_complete(SnowGame snowGame) {
        playSet3_matchPoint(snowGame);
        snowGame.addPoint(TeamType.GUEST);
    }
}
