package com.tonkar.volleyballreferee.business;

import android.content.Context;

import com.tonkar.volleyballreferee.business.data.RecordedGames;
import com.tonkar.volleyballreferee.business.data.SavedRules;
import com.tonkar.volleyballreferee.business.data.SavedTeams;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GeneralService;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.data.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.score.ScoreService;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutService;

public class ServicesProvider {

    private static ServicesProvider sServicesProvider;
    private        GameService      mCurrentGame;
    private        RecordedGames    mRecordedGames;
    private        SavedTeams       mSavedTeams;
    private        SavedRules       mSavedRules;

    private ServicesProvider() {}

    public static ServicesProvider getInstance() {
        if (sServicesProvider == null) {
            sServicesProvider = new ServicesProvider();
        }

        return sServicesProvider;
    }

    public GameService getGameService() { return mCurrentGame; }

    public GeneralService getGeneralService() {
        return mCurrentGame;
    }

    public ScoreService getScoreService() {
        return mCurrentGame;
    }

    public TeamService getTeamService() {
        return mCurrentGame;
    }

    public TimeoutService getTimeoutService() {
        return mCurrentGame;
    }

    public SanctionService getSanctionService() {
        return mCurrentGame;
    }

    public RecordedGamesService getRecordedGamesService(Context context) {
        if (mRecordedGames == null) {
            mRecordedGames = new RecordedGames(context);
        }
        return mRecordedGames;
    }

    public SavedTeamsService getSavedTeamsService(Context context) {
        if (mSavedTeams == null) {
            mSavedTeams = new SavedTeams(context);
        }
        return mSavedTeams;
    }

    public SavedRulesService getSavedRulesService(Context context) {
        if (mSavedRules == null) {
            mSavedRules = new SavedRules(context);
        }
        return mSavedRules;
    }

    public boolean isGameServiceUnavailable() {
        return mCurrentGame == null;
    }

    public void initGameService(GameService gameService) {
        mCurrentGame = gameService;
    }

    public void restoreGameService(Context context) {
        RecordedGamesService recordedGamesService = getRecordedGamesService(context);
        if (recordedGamesService.hasCurrentGame()) {
            initGameService(recordedGamesService.loadCurrentGame());
        }
    }

    public void restoreGameServiceForSetup(Context context) {
        RecordedGamesService recordedGamesService = getRecordedGamesService(context);
        if (recordedGamesService.hasSetupGame()) {
            initGameService(recordedGamesService.loadSetupGame());
        }
    }
}
