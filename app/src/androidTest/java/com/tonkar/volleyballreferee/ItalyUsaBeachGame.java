package com.tonkar.volleyballreferee;

import android.graphics.Color;
import android.util.Log;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.api.ApiLeague;
import com.tonkar.volleyballreferee.api.ApiTeam;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.data.StoredLeagues;
import com.tonkar.volleyballreferee.business.data.StoredTeams;
import com.tonkar.volleyballreferee.business.game.BeachGame;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.interfaces.data.StoredLeaguesService;
import com.tonkar.volleyballreferee.interfaces.data.StoredTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.business.rules.Rules;
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
public class ItalyUsaBeachGame {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private StoredGamesService mStoredGamesService;

    @Test
    public void playGame_manyComplete() {
        for (int index=0; index<80; index++) {
            playGame_technicalTimeout();
        }
    }

    @Test
    public void playGame_complete() {
        Authentication authentication = PrefUtils.getAuthentication(mActivityRule.getActivity());
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame);

        playSet1_complete(beachGame);
        playSet2_complete(beachGame);

        StoredGameService storedGameService = mStoredGamesService.getGame(beachGame.getId());
        ScoreSheetWriter.writeStoredGame(mActivityRule.getActivity(), storedGameService);
    }

    @Test
    public void playGame_matchPoint() {
        Authentication authentication = PrefUtils.getAuthentication(mActivityRule.getActivity());
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
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
            GameService gameService = mStoredGamesService.loadCurrentGame();
            assertNotEquals(null, gameService);
            assertEquals(beachGame, gameService);
        }
    }

    @Test
    public void playGame_technicalTimeout() {
        Authentication authentication = PrefUtils.getAuthentication(mActivityRule.getActivity());
        BeachGame beachGame = GameFactory.createBeachGame(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeamsAndLeague(beachGame);

        playSet1_technicalTimeout(beachGame);
    }

    private void defineTeamsAndLeague(BeachGame beachGame) {
        StoredTeamsService storedTeamsService = new StoredTeams(mActivityRule.getActivity());
        StoredLeaguesService storedLeaguesService = new StoredLeagues(mActivityRule.getActivity());

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
        } else {
            storedTeamsService.copyTeam(teamUsa, beachGame, TeamType.HOME);
        }

        if (teamItaly == null) {
            beachGame.setTeamName(TeamType.GUEST, "ITALY");
            beachGame.setTeamColor(TeamType.GUEST, Color.parseColor("#2980b9"));
        } else {
            storedTeamsService.copyTeam(teamItaly, beachGame, TeamType.GUEST);
        }

        storedTeamsService.createAndSaveTeamFrom(GameType.BEACH, beachGame, TeamType.HOME);
        storedTeamsService.createAndSaveTeamFrom(GameType.BEACH, beachGame, TeamType.GUEST);
        storedLeaguesService.createAndSaveLeagueFrom(beachGame.getLeague());

        beachGame.startMatch();

        mStoredGamesService = new StoredGames(mActivityRule.getActivity());
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
