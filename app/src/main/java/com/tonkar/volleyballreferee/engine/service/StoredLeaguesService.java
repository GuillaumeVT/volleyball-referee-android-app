package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.List;

public interface StoredLeaguesService {

    List<LeagueSummaryDto> listLeagues();

    List<LeagueSummaryDto> listLeagues(GameType kind);

    List<String> listDivisionNames(String id);

    LeagueDto getLeague(GameType kind, String leagueName);

    void createAndSaveLeagueFrom(SelectedLeagueDto selectedLeague);

    void syncLeagues();

    void syncLeagues(DataSynchronizationListener listener);

}
