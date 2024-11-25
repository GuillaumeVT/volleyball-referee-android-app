package com.tonkar.volleyballreferee.engine.game;

import static org.junit.Assert.*;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.api.model.UserSummaryDto;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.*;

import org.junit.*;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(AndroidJUnit4.class)
public class PointsScoreBoardGameTest {

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
        IndoorGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                                                       Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(),
                                                       System.currentTimeMillis(), Rules.officialIndoorRules());
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
            mStoredGamesService.saveCurrentGame(true);
            IGame gameService = mStoredGamesService.loadCurrentGame();
            assertNotEquals(null, gameService);
            assertEquals(game, gameService);
        }

        playSet(game);
    }

    private void defineTeams(IndoorGame game) {
        game.setGender(GenderType.LADIES);

        game.setTeamName(TeamType.HOME, "Team A");
        game.setTeamName(TeamType.GUEST, "Team B");
        game.setTeamColor(TeamType.HOME, Color.parseColor("#052443"));
        game.setTeamColor(TeamType.GUEST, Color.parseColor("#e25618"));

        game.startMatch();

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
