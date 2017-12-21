package com.tonkar.volleyballreferee.interfaces;

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
}
