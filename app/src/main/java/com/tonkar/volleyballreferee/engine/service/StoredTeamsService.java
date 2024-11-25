package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.*;

import java.util.*;

public interface StoredTeamsService {

    List<TeamSummaryDto> listTeams();

    List<TeamSummaryDto> listTeams(GameType kind);

    List<TeamSummaryDto> listTeams(GameType kind, GenderType genderType);

    TeamDto getTeam(String id);

    TeamDto getTeam(GameType kind, String teamName, GenderType gender);

    IBaseTeam createTeam(GameType kind);

    void saveTeam(IBaseTeam team, boolean create);

    void deleteTeam(String id);

    void deleteTeams(Set<String> ids, DataSynchronizationListener listener);

    void createAndSaveTeamFrom(GameType kind, IBaseTeam teamService, TeamType teamType);

    TeamDto copyTeam(IBaseTeam teamService);

    IBaseTeam copyTeam(TeamDto team);

    void copyTeam(TeamDto source, IBaseTeam dest, TeamType teamType);

    void copyTeam(IBaseTeam source, TeamDto dest, TeamType teamType);

    void syncTeams();

    void syncTeams(DataSynchronizationListener listener);
}
