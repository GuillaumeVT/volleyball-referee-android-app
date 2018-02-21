package com.tonkar.volleyballreferee.business;

import android.content.Context;

import com.tonkar.volleyballreferee.business.data.RecordedGames;
import com.tonkar.volleyballreferee.business.data.SavedTeams;
import com.tonkar.volleyballreferee.interfaces.GameService;
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

    private ServicesProvider() {}

    public static ServicesProvider getInstance() {
        if (sServicesProvider == null) {
            sServicesProvider = new ServicesProvider();
        }

        return sServicesProvider;
    }

    public GameService getGameService() { return mCurrentGame; }

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

    public RecordedGamesService getRecordedGamesService() {
        return mRecordedGames;
    }

    public SavedTeamsService getSavedTeamsService() {
        return mSavedTeams;
    }

    private boolean isGameServiceAvailable() {
        return mCurrentGame != null;
    }

    private boolean isRecordedGamesServiceAvailable() {
        return mRecordedGames != null;
    }

    private boolean isSavedTeamsServiceAvailable() {
        return mSavedTeams != null;
    }

    public boolean areServicesAvailable() {
        return isGameServiceAvailable() && isRecordedGamesServiceAvailable() && isSavedTeamsServiceAvailable();
    }

    public void initGameService(GameService gameService) {
        mCurrentGame = gameService;
    }

    public void restoreGameService(Context context) {
        restoreRecordedGamesService(context);
        restoreSavedTeamsService(context);
        if (mRecordedGames.hasCurrentGame()) {
            initGameService(mRecordedGames.loadCurrentGame());
        }
    }

    public void restoreGameServiceForSetup(Context context) {
        restoreRecordedGamesService(context);
        restoreSavedTeamsService(context);
        if (mRecordedGames.hasSetupGame()) {
            initGameService(mRecordedGames.loadSetupGame());
        }
    }

    public void restoreRecordedGamesService(Context context) {
        if (!isRecordedGamesServiceAvailable()) {
            mRecordedGames = new RecordedGames(context);
            mRecordedGames.loadRecordedGames();
        }
        restoreSavedTeamsService(context);
    }

    public void restoreSavedTeamsService(Context context) {
        if (!isSavedTeamsServiceAvailable()) {
            mSavedTeams = new SavedTeams(context);
            mSavedTeams.loadSavedTeams();
        }
    }
}
