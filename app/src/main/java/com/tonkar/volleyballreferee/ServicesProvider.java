package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.interfaces.ScoreClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.ScoreService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TimeoutClient;
import com.tonkar.volleyballreferee.interfaces.TimeoutService;

public class ServicesProvider implements ScoreClient, TeamClient, TimeoutClient, GamesHistoryClient {

    private static ServicesProvider    sServicesProvider;
    private        ScoreService        mScoreService;
    private        TeamService         mTeamService;
    private        TimeoutService      mTimeoutService;
    private        GamesHistoryService mGamesHistoryService;

    private ServicesProvider() {}

    public static ServicesProvider getInstance() {
        if (sServicesProvider == null) {
            sServicesProvider = new ServicesProvider();
        }

        return sServicesProvider;
    }

    public ScoreService getScoreService() {
        return mScoreService;
    }

    public TeamService getTeamService() {
        return mTeamService;
    }

    public TimeoutService getTimeoutService() {
        return mTimeoutService;
    }

    public GamesHistoryService getGameHistoryService() {
        return mGamesHistoryService;
    }

    @Override
    public void setScoreService(ScoreService scoreService) {
        mScoreService = scoreService;
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public void setTimeoutService(TimeoutService timeoutService) {
        mTimeoutService = timeoutService;
    }

    @Override
    public void setGamesHistoryService(GamesHistoryService gamesHistoryService) {
        mGamesHistoryService = gamesHistoryService;
    }
}
