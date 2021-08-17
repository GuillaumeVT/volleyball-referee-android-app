package com.tonkar.volleyballreferee;

import android.content.Context;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.api.model.ApiLeague;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.game.GameFactory;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.TimeBasedGame;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.service.StoredGamesService;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesManager;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesService;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class TimeBasedGameTest {

    private Context mContext;

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void playGame_complete() {
        ApiUserSummary user = PrefUtils.getUser(mContext);
        TimeBasedGame game = GameFactory.createTimeBasedGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime(), System.currentTimeMillis());

        defineTeamsAndLeague(game);

        game.setDuration(250);
        game.start();

        while (game.getRemainingTime() > 0L) {
            game.addPoint(TeamType.HOME);
            game.addPoint(TeamType.GUEST);
        }

        game.stop();
    }

    private void defineTeamsAndLeague(TimeBasedGame game) {
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mContext);

        game.setGender(GenderType.MIXED);

        ApiLeague league = storedLeaguesService.getLeague(GameType.TIME, "Tournament X");
        if (league == null) {
            game.getLeague().setName("Tournament X");
            game.getLeague().setDivision("Pool 4");
        } else {
            game.getLeague().setId(league.getId());
            game.getLeague().setCreatedBy(league.getCreatedBy());
            game.getLeague().setCreatedAt(league.getCreatedAt());
            game.getLeague().setUpdatedAt(league.getUpdatedAt());
            game.getLeague().setName(league.getName());
            game.getLeague().setDivision("Pool 4");
        }

        game.setTeamName(TeamType.HOME, "Team 1");
        game.setTeamName(TeamType.GUEST, "Team 2");
        game.setTeamColor(TeamType.HOME, Color.parseColor("#006032"));
        game.setTeamColor(TeamType.GUEST, Color.parseColor("#ffffff"));

        storedLeaguesService.createAndSaveLeagueFrom(game.getLeague());

        game.startMatch();

        StoredGamesService storedGamesService = new StoredGamesManager(mContext);
        storedGamesService.connectGameRecorder(game);
    }

}
