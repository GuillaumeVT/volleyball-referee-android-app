package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.List;

public interface StoredLeaguesService {

    List<ApiLeagueSummary> listLeagues();

    List<ApiLeagueSummary> listLeagues(GameType kind);

    List<String> listDivisionNames(String id);

    ApiLeague getLeague(GameType kind, String leagueName);

    void createAndSaveLeagueFrom(ApiSelectedLeague selectedLeague);

    void syncLeagues();

    void syncLeagues(DataSynchronizationListener listener);

}
