package com.tonkar.volleyballreferee;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
import android.test.suitebuilder.annotation.LargeTest;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.game.BeachGame;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.data.PdfGameWriter;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ItalyUsaBeachGame {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void playGame_complete() {
        BeachGame beachGame = GameFactory.createBeachGame();

        defineTeams(beachGame);

        playSet1_complete(beachGame);
        playSet2_complete(beachGame);

        RecordedGameService recordedGameService = ServicesProvider.getInstance().getRecordedGamesService().getRecordedGameService(beachGame.getGameDate());
        PdfGameWriter.writeRecordedGame(mActivityRule.getActivity(), recordedGameService);
    }

    @Test
    public void playGame_matchPoint() {
        BeachGame beachGame = GameFactory.createBeachGame();

        defineTeams(beachGame);

        playSet1_complete(beachGame);
        playSet2_matchPoint(beachGame);
    }

    @Test
    public void playGame_technicalTimeout() {
        BeachGame beachGame = GameFactory.createBeachGame();

        defineTeams(beachGame);

        playSet1_technicalTimeout(beachGame);
    }

    private void defineTeams(BeachGame beachGame) {
        beachGame.setGenderType(GenderType.GENTS);
        beachGame.setLeagueName("FIVB Beach Volleyball World Championship 2017");
        beachGame.setTeamName(TeamType.HOME, "USA");
        beachGame.setTeamName(TeamType.GUEST, "ITALY");
        beachGame.setTeamColor(TeamType.HOME, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt8));
        beachGame.setTeamColor(TeamType.GUEST, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt1));
        beachGame.initTeams();

        ServicesProvider.getInstance().getRecordedGamesService().connectGameRecorder();
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
