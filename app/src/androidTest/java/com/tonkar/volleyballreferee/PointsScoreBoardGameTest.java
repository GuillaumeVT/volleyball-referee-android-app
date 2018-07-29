package com.tonkar.volleyballreferee;

import android.graphics.Color;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.IndoorGame;
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

        IndoorGame loadedGame = (IndoorGame) ServicesProvider.getInstance().getRecordedGamesService().loadCurrentGame();
        assertEquals(game, loadedGame);

        playSet(game);

        RecordedGameService recordedGameService = ServicesProvider.getInstance().getRecordedGamesService().getRecordedGameService(game.getGameDate());
        ScoreSheetWriter.writeRecordedGame(mActivityRule.getActivity(), recordedGameService);
    }

    private void defineTeams(IndoorGame game) {
        game.setGenderType(GenderType.LADIES);

        game.setTeamName(TeamType.HOME, "Team A");
        game.setTeamName(TeamType.GUEST, "Team B");
        game.setTeamColor(TeamType.HOME, Color.parseColor("#052443"));
        game.setTeamColor(TeamType.GUEST, Color.parseColor("#e25618"));

        game.startMatch();

        ServicesProvider.getInstance().getRecordedGamesService().connectGameRecorder();
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
