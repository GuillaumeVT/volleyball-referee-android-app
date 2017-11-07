package com.tonkar.volleyballreferee.interfaces;

public interface RecordedGameService extends BaseGameService, BaseTeamService {

    boolean matchesFilter(String text);

}
