package com.tonkar.volleyballreferee;

import android.graphics.Color;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.data.RecordedGames;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.game.Indoor4x4Game;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class Indoor4x4CompleteGame {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private RecordedGamesService mRecordedGamesService;

    @Test
    public void playGame_complete() {
        Indoor4x4Game indoor4x4Game = GameFactory.createIndoor4x4Game(System.currentTimeMillis(), System.currentTimeMillis(), Rules.defaultIndoor4x4Rules());

        defineTeams(indoor4x4Game);

        composeTeams(indoor4x4Game);
        playSet_complete(indoor4x4Game);

        composeTeams(indoor4x4Game);
        playSet_complete(indoor4x4Game);

        composeTeams(indoor4x4Game);
        playSet_complete(indoor4x4Game);

        RecordedGameService recordedGameService = mRecordedGamesService.getRecordedGameService(indoor4x4Game.getGameDate());
        ScoreSheetWriter.writeRecordedGame(mActivityRule.getActivity(), recordedGameService);
    }

    private void defineTeams(Indoor4x4Game indoor4x4Game) {
        indoor4x4Game.setGenderType(GenderType.GENTS);

        indoor4x4Game.setLeagueName("4x4");
        indoor4x4Game.setDivisionName("Division 1");

        indoor4x4Game.setTeamName(TeamType.HOME, "Home Team");
        indoor4x4Game.setTeamName(TeamType.GUEST, "Guest Team");
        indoor4x4Game.setTeamColor(TeamType.HOME, Color.parseColor("#2980b9"));
        indoor4x4Game.setTeamColor(TeamType.GUEST, Color.parseColor("#c2185b"));

        for (int index = 1; index <= 8; index++) {
            indoor4x4Game.addPlayer(TeamType.HOME, index);
            indoor4x4Game.addPlayer(TeamType.GUEST, index);
        }

        indoor4x4Game.setCaptain(TeamType.HOME, 1);
        indoor4x4Game.setCaptain(TeamType.GUEST, 2);

        indoor4x4Game.startMatch();

        mRecordedGamesService = new RecordedGames(mActivityRule.getActivity());
        mRecordedGamesService.connectGameRecorder(indoor4x4Game);
    }

    private void composeTeams(Indoor4x4Game indoor4x4Game) {
        for (int index = 1; index <= 4; index++) {
            indoor4x4Game.substitutePlayer(TeamType.HOME, index, PositionType.fromInt(index), ActionOriginType.USER);
            indoor4x4Game.substitutePlayer(TeamType.GUEST, 5 - index, PositionType.fromInt(5 - index), ActionOriginType.USER);
        }

        indoor4x4Game.confirmStartingLineup();
    }

    private void playSet_complete(Indoor4x4Game indoor4x4Game) {
        indoor4x4Game.swapServiceAtStart();

        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.callTimeout(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.giveSanction(TeamType.GUEST, SanctionType.YELLOW, 2);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.callTimeout(TeamType.GUEST);
        indoor4x4Game.substitutePlayer(TeamType.GUEST, 5, PositionType.POSITION_1, ActionOriginType.USER);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.substitutePlayer(TeamType.HOME, 6, PositionType.POSITION_3, ActionOriginType.USER);
        indoor4x4Game.substitutePlayer(TeamType.HOME, 8, PositionType.POSITION_1, ActionOriginType.USER);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
        indoor4x4Game.addPoint(TeamType.GUEST);
        indoor4x4Game.addPoint(TeamType.HOME);
    }

}