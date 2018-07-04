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

    public RecordedGamesService getRecordedGamesService() {
        return mRecordedGames;
    }

    public SavedTeamsService getSavedTeamsService() {
        return mSavedTeams;
    }

    public SavedRulesService getSavedRulesService() {
        return mSavedRules;
    }

    public boolean isGameServiceUnavailable() {
        return mCurrentGame == null || isRecordedGamesServiceUnavailable();
    }

    public boolean areSetupServicesUnavailable() {
        return mCurrentGame == null
                || isRecordedGamesServiceUnavailable()
                || isSavedTeamsServiceUnavailable()
                || isSavedRulesServiceUnavailable();
    }

    private boolean isRecordedGamesServiceUnavailable() {
        return mRecordedGames == null;
    }

    public boolean isSavedTeamsServiceUnavailable() {
        return mSavedTeams == null;
    }

    public boolean isSavedRulesServiceUnavailable() {
        return mSavedRules == null;
    }

    public void initGameService(GameService gameService) {
        mCurrentGame = gameService;
    }

    public void restoreGameService(Context context) {
        restoreRecordedGamesService(context);
        if (mRecordedGames.hasCurrentGame()) {
            initGameService(mRecordedGames.loadCurrentGame());
        }
    }

    public void restoreGameServiceForSetup(Context context) {
        restoreRecordedGamesService(context);
        restoreSavedTeamsService(context);
        restoreSavedRulesService(context);
        if (mRecordedGames.hasSetupGame()) {
            initGameService(mRecordedGames.loadSetupGame());
        }
    }

    public void restoreRecordedGamesService(Context context) {
        if (isRecordedGamesServiceUnavailable()) {
            mRecordedGames = new RecordedGames(context);
            mRecordedGames.loadRecordedGames();
        }
    }

    public void restoreSavedTeamsService(Context context) {
        if (isSavedTeamsServiceUnavailable()) {
            mSavedTeams = new SavedTeams(context);
            mSavedTeams.loadSavedTeams();
        }
    }

    public void restoreSavedRulesService(Context context) {
        if (isSavedRulesServiceUnavailable()) {
            mSavedRules = new SavedRules(context);
            mSavedRules.loadSavedRules();
        }
    }

    public void restoreAllServicesAndSync(Context context) {
        if (isSavedRulesServiceUnavailable()) {
            restoreSavedRulesService(context);
        } else {
            getSavedRulesService().syncRulesOnline();
        }
        if (isSavedTeamsServiceUnavailable()) {
            restoreSavedTeamsService(context);
        } else {
            getSavedTeamsService().syncTeamsOnline();
        }
        if (isRecordedGamesServiceUnavailable()) {
            restoreRecordedGamesService(context);
        } else {
            getRecordedGamesService().syncGamesOnline();
        }
    }
}
