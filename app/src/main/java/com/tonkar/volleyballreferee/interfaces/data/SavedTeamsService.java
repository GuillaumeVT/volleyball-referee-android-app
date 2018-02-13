package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;

import java.util.List;

public interface SavedTeamsService {

    String SAVED_TEAMS_FILE = "device_saved_teams.json";

    void loadSavedTeams();

    List<BaseIndoorTeamService> getSavedTeamServiceList();

    BaseIndoorTeamService getSavedTeamService(String teamName, GenderType genderType);

    void createTeam();

    void editTeam(String teamName, GenderType genderType);

    BaseIndoorTeamService getCurrentTeam();

    void saveCurrentTeam();

    void deleteSavedTeam(String teamName, GenderType genderType);

    void deleteAllSavedTeams();

    void createAndSaveTeamFrom(BaseIndoorTeamService indoorTeamService, TeamType teamType);

    void copyTeam(BaseIndoorTeamService source, BaseIndoorTeamService dest, TeamType teamType);
}
