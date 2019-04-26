package com.tonkar.volleyballreferee;

import android.graphics.Color;
import android.util.Log;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.IndoorGame;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.business.rules.Rules;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class PointsScoreBoardGameTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private StoredGamesService mStoredGamesService;

    @Test
    public void playGame_complete() {
        IndoorGame game = GameFactory.createIndoorGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialIndoorRules());
        game.setUsage(UsageType.POINTS_SCOREBOARD);

        defineTeams(game);
        playSet(game);
        playSet(game);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int index = 0; index < 5; index++) {
            Log.i("VBR-Test", "playGame_complete index #" + index);
            mStoredGamesService.saveCurrentGame();
            GameService gameService = mStoredGamesService.loadCurrentGame();
            assertNotEquals(null, gameService);
            assertEquals(game, gameService);
        }

        playSet(game);

        StoredGameService storedGameService = mStoredGamesService.getGame(game.getGameDate());
        ScoreSheetWriter.writeRecordedGame(mActivityRule.getActivity(), storedGameService);
    }

    private void defineTeams(IndoorGame game) {
        game.setGender(GenderType.LADIES);

        game.setTeamName(TeamType.HOME, "Team A");
        game.setTeamName(TeamType.GUEST, "Team B");
        game.setTeamColor(TeamType.HOME, Color.parseColor("#052443"));
        game.setTeamColor(TeamType.GUEST, Color.parseColor("#e25618"));

        game.startMatch();

        mStoredGamesService = new StoredGames(mActivityRule.getActivity());
        mStoredGamesService.connectGameRecorder(game);
    }

    private void playSet(IndoorGame indoorGame) {
        for (int index = 0; index < 23; index++) {
            indoorGame.addPoint(TeamType.HOME);
            indoorGame.addPoint(TeamType.GUEST);
        }

        indoorGame.addPoint(TeamType.HOME);
        indoorGame.addPoint(TeamType.HOME);
    }

}
