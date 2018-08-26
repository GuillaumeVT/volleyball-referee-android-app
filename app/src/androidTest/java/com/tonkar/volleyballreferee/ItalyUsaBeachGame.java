package com.tonkar.volleyballreferee;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.game.BeachGame;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ItalyUsaBeachGame {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void playGame_complete() {
        BeachGame beachGame = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeams(beachGame);

        playSet1_complete(beachGame);
        playSet2_complete(beachGame);

        RecordedGameService recordedGameService = ServicesProvider.getInstance().getRecordedGamesService(mActivityRule.getActivity().getApplicationContext()).getRecordedGameService(beachGame.getGameDate());
        ScoreSheetWriter.writeRecordedGame(mActivityRule.getActivity(), recordedGameService);
    }

    @Test
    public void playGame_matchPoint() {
        BeachGame beachGame = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeams(beachGame);

        playSet1_complete(beachGame);
        playSet2_matchPoint(beachGame);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Context applicationContext = mActivityRule.getActivity().getApplicationContext();

        for (int index = 0; index < 5; index++) {
            Log.i("VBR-Test", "playGame_matchPoint index #" + index);
            ServicesProvider.getInstance().getRecordedGamesService(applicationContext).saveCurrentGame();
            GameService gameService = ServicesProvider.getInstance().getRecordedGamesService(applicationContext).loadCurrentGame();
            assertNotEquals(null, gameService);
            assertEquals(beachGame, gameService);
        }
    }

    @Test
    public void playGame_technicalTimeout() {
        BeachGame beachGame = GameFactory.createBeachGame(System.currentTimeMillis(), System.currentTimeMillis(), Rules.officialBeachRules());

        defineTeams(beachGame);

        playSet1_technicalTimeout(beachGame);
    }

    private void defineTeams(BeachGame beachGame) {
        beachGame.setGenderType(GenderType.GENTS);
        beachGame.setLeagueName("FIVB Beach Volleyball World Championship 2017");
        beachGame.setDivisionName("Final");
        beachGame.setTeamName(TeamType.HOME, "USA");
        beachGame.setTeamName(TeamType.GUEST, "ITALY");
        beachGame.setTeamColor(TeamType.HOME, Color.parseColor("#bc0019"));
        beachGame.setTeamColor(TeamType.GUEST, Color.parseColor("#2980b9"));
        beachGame.startMatch();

        ServicesProvider.getInstance().getRecordedGamesService(mActivityRule.getActivity().getApplicationContext()).connectGameRecorder();
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
