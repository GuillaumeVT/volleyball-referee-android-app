package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiLeague;
import com.tonkar.volleyballreferee.api.ApiLeagueDescription;
import com.tonkar.volleyballreferee.api.ApiSelectedLeague;
import com.tonkar.volleyballreferee.interfaces.GameType;

import java.util.List;

public interface StoredLeaguesService {

    List<ApiLeagueDescription> listLeagues();

    List<ApiLeagueDescription> listLeagues(GameType kind);

    List<String> listDivisionNames(String id);

    ApiLeague getLeague(String id);

    ApiLeague getLeague(GameType kind, String leagueName);

    void createAndSaveLeagueFrom(ApiSelectedLeague selectedLeague);

    void syncLeagues();

    void syncLeagues(DataSynchronizationListener listener);

}
