package com.tonkar.volleyballreferee.interfaces.team;

import java.util.Set;

public interface BaseTeamService {

    String getRefereeName();

    void setRefereeName(String name);

    String getLeagueName();

    void setLeagueName(String name);

    String getTeamName(TeamType teamType);

    int getTeamColor(TeamType teamType);

    void setTeamName(TeamType teamType, String name);

    void setTeamColor(TeamType teamType, int color);

    void addPlayer(TeamType teamType, int number);

    void removePlayer(TeamType teamType, int number);

    boolean hasPlayer(TeamType teamType, int number);

    int getNumberOfPlayers(TeamType teamType);

    Set<Integer> getPlayers(TeamType teamType);

    GenderType getGenderType();

    GenderType getGenderType(TeamType teamType);

    void setGenderType(GenderType genderType);

    void setGenderType(TeamType teamType, GenderType genderType);

    int getExpectedNumberOfPlayersOnCourt();

}
