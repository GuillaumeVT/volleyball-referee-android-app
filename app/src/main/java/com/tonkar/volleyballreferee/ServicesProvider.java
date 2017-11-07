package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.interfaces.GameClient;
import com.tonkar.volleyballreferee.interfaces.GameHistoryClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TimeoutClient;
import com.tonkar.volleyballreferee.interfaces.TimeoutService;

public class ServicesProvider implements GameClient, TeamClient, TimeoutClient, GameHistoryClient {

    private static ServicesProvider    sServicesProvider;
    private        GameService         mGameService;
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

    public GameService getGameService() {
        return mGameService;
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
    public void setGameService(GameService gameService) {
        mGameService = gameService;
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
    public void setGameHistoryService(GamesHistoryService gamesHistoryService) {
        mGamesHistoryService = gamesHistoryService;
    }
}
