package com.tonkar.volleyballreferee.engine.stored;

import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.stored.api.ApiLeague;
import com.tonkar.volleyballreferee.engine.stored.api.ApiLeagueSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSelectedLeague;

import java.util.List;

public interface StoredLeaguesService {

    List<ApiLeagueSummary> listLeagues();

    List<ApiLeagueSummary> listLeagues(GameType kind);

    List<String> listDivisionNames(String id);

    ApiLeague getLeague(String id);

    ApiLeague getLeague(GameType kind, String leagueName);

    void createAndSaveLeagueFrom(ApiSelectedLeague selectedLeague);

    void syncLeagues();

    void syncLeagues(DataSynchronizationListener listener);

}
