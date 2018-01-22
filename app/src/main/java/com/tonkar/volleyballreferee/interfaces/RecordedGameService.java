package com.tonkar.volleyballreferee.interfaces;

public interface RecordedGameService extends BaseScoreService, BaseIndoorTeamService, BaseTimeoutService, BaseTimeService {

    boolean matchesFilter(String text);

}
