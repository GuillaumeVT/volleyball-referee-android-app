package com.tonkar.volleyballreferee.interfaces;

public interface RecordedGameService extends BaseScoreService, BaseIndoorTeamService, BaseTimeoutService, BaseTimeService, WebGameService {

    boolean matchesFilter(String text);

}
