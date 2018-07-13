package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.business.data.RecordedTeam;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;

import java.util.List;

public interface SavedTeamsService {

    String SAVED_TEAMS_FILE = "device_saved_teams.json";

    void loadSavedTeams();

    List<RecordedTeam> getSavedTeamList();

    List<RecordedTeam> getSavedTeamList(GameType gameType);

    List<String> getSavedTeamNameList(GameType gameType, GenderType genderType);

    RecordedTeam getSavedTeam(GameType gameType, String teamName, GenderType genderType);

    void createTeam(GameType gameType);

    void editTeam(GameType gameType, String teamName, GenderType genderType);

    BaseTeamService getCurrentTeam();

    void saveCurrentTeam();

    void deleteSavedTeam(GameType gameType, String teamName, GenderType genderType);

    void deleteAllSavedTeams();

    void createAndSaveTeamFrom(GameType gameType, BaseTeamService teamService, TeamType teamType);

    void copyTeam(RecordedTeam source, BaseTeamService dest, TeamType teamType);

    void copyTeam(BaseTeamService source, RecordedTeam dest, TeamType teamType);

    void syncTeamsOnline();

    void syncTeamsOnline(DataSynchronizationListener listener);
}
