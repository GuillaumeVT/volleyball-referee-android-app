package com.tonkar.volleyballreferee.engine.game.score;

import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.List;

public interface IBaseScore {

    String getGameSummary();

    int getNumberOfSets();

    int getSets(TeamType teamType);

    int getPoints(TeamType teamType);

    int getPoints(TeamType teamType, int setIndex);

    List<TeamType> getPointsLadder();

    List<TeamType> getPointsLadder(int setIndex);

    TeamType getServingTeam();

    TeamType getServingTeam(int setIndex);

    TeamType getFirstServingTeam();

    TeamType getFirstServingTeam(int setIndex);

    String getScore();

    long getSetDuration(int setIndex);

    long getSetStartTime(int setIndex);

    long getSetEndTime(int setIndex);
}
