package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.*;

import java.util.*;

public interface StoredTeamsService {

    List<ApiTeamSummary> listTeams();

    List<ApiTeamSummary> listTeams(GameType kind);

    List<ApiTeamSummary> listTeams(GameType kind, GenderType genderType);

    ApiTeam getTeam(String id);

    ApiTeam getTeam(GameType kind, String teamName, GenderType gender);

    IBaseTeam createTeam(GameType kind);

    void saveTeam(IBaseTeam team, boolean create);

    void deleteTeam(String id);

    void deleteTeams(Set<String> ids, DataSynchronizationListener listener);

    void createAndSaveTeamFrom(GameType kind, IBaseTeam teamService, TeamType teamType);

    ApiTeam copyTeam(IBaseTeam teamService);

    IBaseTeam copyTeam(ApiTeam team);

    void copyTeam(ApiTeam source, IBaseTeam dest, TeamType teamType);

    void copyTeam(IBaseTeam source, ApiTeam dest, TeamType teamType);

    void syncTeams();

    void syncTeams(DataSynchronizationListener listener);
}
