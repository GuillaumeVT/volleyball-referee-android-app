package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiTeam;
import com.tonkar.volleyballreferee.api.ApiTeamDescription;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;

import java.util.List;

public interface StoredTeamsService {

    boolean hasTeams();

    List<ApiTeamDescription> listTeams();

    List<ApiTeamDescription> listTeams(GameType kind);

    List<ApiTeamDescription> listTeams(GameType kind, GenderType genderType);

    ApiTeam getTeam(String id);

    ApiTeam getTeam(GameType kind, String teamName, GenderType gender);

    ApiTeam readTeam(String json);

    String writeTeam(ApiTeam team);

    BaseTeamService createTeam(GameType kind);

    void saveTeam(BaseTeamService team);

    void deleteTeam(String id);

    void deleteAllTeams();

    void createAndSaveTeamFrom(GameType kind, BaseTeamService teamService, TeamType teamType);

    ApiTeam copyTeam(BaseTeamService teamService);

    BaseTeamService copyTeam(ApiTeam team);

    void copyTeam(ApiTeam source, BaseTeamService dest, TeamType teamType);

    void copyTeam(BaseTeamService source, ApiTeam dest, TeamType teamType);

    void syncTeams();

    void syncTeams(DataSynchronizationListener listener);
}
