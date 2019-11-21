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
import com.tonkar.volleyballreferee.engine.game.SnowGame;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.engine.stored.ScoreSheetWriter;
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
public class RussiaUsaSnowGame {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private StoredGamesService mStoredGamesService;

    @Test
    public void playGame_complete() {
        ApiUserSummary user = PrefUtils.getUser(mActivityRule.getActivity());
        SnowGame snowGame = GameFactory.createSnowGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialSnowRules());

        defineTeamsAndLeague(snowGame);

        playSet1_complete(snowGame);
        playSet2_complete(snowGame);
        playSet3_complete(snowGame);

        IStoredGame storedGame = mStoredGamesService.getGame(snowGame.getId());
        ScoreSheetWriter.writeStoredGame(mActivityRule.getActivity(), storedGame);
    }

    @Test
    public void playGame_matchPoint() {
        ApiUserSummary user = PrefUtils.getUser(mActivityRule.getActivity());
        SnowGame snowGame = GameFactory.createSnowGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialSnowRules());

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
        StoredTeamsService storedTeamsService = new StoredTeamsManager(mActivityRule.getActivity());
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mActivityRule.getActivity());

        snowGame.setGender(GenderType.GENTS);

        ApiLeague league = storedLeaguesService.getLeague(GameType.SNOW, "FIVB Snow Volleyball Kronplatz 2019");
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

        ApiTeam teamRussia = storedTeamsService.getTeam(GameType.SNOW, "RUSSIA 1", GenderType.GENTS);
        ApiTeam teamUsa = storedTeamsService.getTeam(GameType.SNOW, "USA 2", GenderType.GENTS);

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

        mStoredGamesService = new StoredGamesManager(mActivityRule.getActivity());
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
