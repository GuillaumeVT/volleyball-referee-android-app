package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface BaseGameService {

    GameType getGameType();

    long getGameDate();

    String getGameSummary();

    int getNumberOfSets();

    int getSets(TeamType teamType);

    long getSetDuration();

    long getSetDuration(int setIndex);

    int getPoints(TeamType teamType);

    int getPoints(TeamType teamType, int setIndex);

    List<TeamType> getPointsLadder();

    List<TeamType> getPointsLadder(int setIndex);
}
