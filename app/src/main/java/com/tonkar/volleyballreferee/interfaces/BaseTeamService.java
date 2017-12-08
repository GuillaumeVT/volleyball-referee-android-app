package com.tonkar.volleyballreferee.interfaces;

import java.util.Set;

public interface BaseTeamService {

    String getTeamName(TeamType teamType);

    int getTeamColor(TeamType teamType);

    void setTeamName(TeamType teamType, String name);

    void setTeamColor(TeamType teamType, int color);

    void addPlayer(TeamType teamType, int number);

    void removePlayer(TeamType teamType, int number);

    boolean hasPlayer(TeamType teamType, int number);

    int getNumberOfPlayers(TeamType teamType);

    Set<Integer> getPlayers(TeamType teamType);

    void initTeams();

    UsageType getUsageType();

    void setUsageType(UsageType usageType);

}
