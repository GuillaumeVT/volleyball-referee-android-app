package com.tonkar.volleyballreferee;

import android.graphics.Color;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.TimeBasedGame;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TimeBasedGameTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private StoredGamesService mStoredGamesService;

    @Test
    public void playGame_complete() {
        TimeBasedGame game = GameFactory.createTimeBasedGame(System.currentTimeMillis(), System.currentTimeMillis());

        defineTeams(game);

        game.setDuration(250);
        game.start();

        while (game.getRemainingTime() > 0L) {
            game.addPoint(TeamType.HOME);
            game.addPoint(TeamType.GUEST);
        }

        game.stop();

        StoredGameService storedGameService = mStoredGamesService.getGame(game.getGameDate());
        ScoreSheetWriter.writeRecordedGame(mActivityRule.getActivity(), storedGameService);
    }

    private void defineTeams(TimeBasedGame game) {
        game.setGender(GenderType.MIXED);

        game.setLeagueName("Tournament X");
        game.setDivisionName("Pool 4");

        game.setTeamName(TeamType.HOME, "Team 1");
        game.setTeamName(TeamType.GUEST, "Team 2");
        game.setTeamColor(TeamType.HOME, Color.parseColor("#006032"));
        game.setTeamColor(TeamType.GUEST, Color.parseColor("#ffffff"));

        game.startMatch();

        mStoredGamesService = new StoredGames(mActivityRule.getActivity());
        mStoredGamesService.connectGameRecorder(game);
    }

}
