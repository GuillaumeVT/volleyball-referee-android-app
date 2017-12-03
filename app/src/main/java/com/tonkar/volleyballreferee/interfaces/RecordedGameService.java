package com.tonkar.volleyballreferee.interfaces;

public interface RecordedGameService extends BaseScoreService, BaseIndoorTeamService, BaseTimeoutService {

    boolean matchesFilter(String text);

}
