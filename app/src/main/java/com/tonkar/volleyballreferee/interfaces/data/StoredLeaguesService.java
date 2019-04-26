package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiLeague;
import com.tonkar.volleyballreferee.interfaces.GameType;

import java.util.List;

public interface StoredLeaguesService {

    List<ApiLeague> listLeagues();

    List<String> listLeagueNames(GameType kind);

    List<String> listDivisionNames(GameType kind, String leagueName);

    void createAndSaveLeagueFrom(GameType kind, String leagueName, String divisionName);

    void syncLeagues();

    void syncLeagues(DataSynchronizationListener listener);

}
