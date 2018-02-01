package com.tonkar.volleyballreferee;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.IndoorGame;
import com.tonkar.volleyballreferee.business.data.PdfGameWriter;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.UsageType;
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
        IndoorGame indoorGame = GameFactory.createIndoorGame();
        indoorGame.setUsageType(UsageType.POINTS_SCOREBOARD);

        defineTeams(indoorGame);
        playSet(indoorGame);
        playSet(indoorGame);

        IndoorGame loadedGame = (IndoorGame) ServicesProvider.getInstance().getRecordedGamesService().loadCurrentGame();
        assertEquals(indoorGame, loadedGame);

        playSet(indoorGame);

        RecordedGameService recordedGameService = ServicesProvider.getInstance().getRecordedGamesService().getRecordedGameService(indoorGame.getGameDate());
        PdfGameWriter.writeRecordedGame(mActivityRule.getActivity(), recordedGameService);
    }

    private void defineTeams(IndoorGame indoorGame) {
        indoorGame.setGenderType(GenderType.LADIES);

        indoorGame.setTeamName(TeamType.HOME, "Team A");
        indoorGame.setTeamName(TeamType.GUEST, "Team B");
        indoorGame.setTeamColor(TeamType.HOME, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt4));
        indoorGame.setTeamColor(TeamType.GUEST, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt9));

        indoorGame.initTeams();

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
