package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface BaseScoreService {

    GameType getGameType();

    long getGameDate();

    String getGameSummary();

    int getNumberOfSets();

    int getSets(TeamType teamType);

    long getSetDuration(int setIndex);

    int getPoints(TeamType teamType);

    int getPoints(TeamType teamType, int setIndex);

    List<TeamType> getPointsLadder();

    List<TeamType> getPointsLadder(int setIndex);

    TeamType getServingTeam();

    TeamType getServingTeam(int setIndex);

    boolean isMatchCompleted();

    UsageType getUsageType();

    void setUsageType(UsageType usageType);
}
