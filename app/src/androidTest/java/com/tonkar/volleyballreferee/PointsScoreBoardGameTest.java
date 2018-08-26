package com.tonkar.volleyballreferee;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.IndoorGame;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.rules.Rules;
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

    @Test
    public void playGame_complete() {
        IndoorGame game = GameFactory.createIndoorGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialIndoorRules());
        game.setUsageType(UsageType.POINTS_SCOREBOARD);

        defineTeams(game);
        playSet(game);
        playSet(game);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Context applicationContext = mActivityRule.getActivity().getApplicationContext();

        for (int index = 0; index < 5; index++) {
            Log.i("VBR-Test", "playGame_complete index #" + index);
            ServicesProvider.getInstance().getRecordedGamesService(applicationContext).saveCurrentGame();
            GameService gameService = ServicesProvider.getInstance().getRecordedGamesService(applicationContext).loadCurrentGame();
            assertNotEquals(null, gameService);
            assertEquals(game, gameService);
        }

        playSet(game);

        RecordedGameService recordedGameService = ServicesProvider.getInstance().getRecordedGamesService(mActivityRule.getActivity().getApplicationContext()).getRecordedGameService(game.getGameDate());
        ScoreSheetWriter.writeRecordedGame(mActivityRule.getActivity(), recordedGameService);
    }

    private void defineTeams(IndoorGame game) {
        game.setGenderType(GenderType.LADIES);

        game.setTeamName(TeamType.HOME, "Team A");
        game.setTeamName(TeamType.GUEST, "Team B");
        game.setTeamColor(TeamType.HOME, Color.parseColor("#052443"));
        game.setTeamColor(TeamType.GUEST, Color.parseColor("#e25618"));

        game.startMatch();

        ServicesProvider.getInstance().getRecordedGamesService(mActivityRule.getActivity().getApplicationContext()).connectGameRecorder();
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
