package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface IndoorTeamService extends TeamService {

    int getLiberoColor(TeamType teamType);

    void setLiberoColor(TeamType teamType, int colorId);

    int getNumberOfPlayers(TeamType teamType);

    void addLibero(TeamType teamType, int number);

    void removeLibero(TeamType teamType, int number);

    boolean isLibero(TeamType teamType, int number);

    boolean canAddLibero(TeamType teamType);

    List<Integer> getPossibleReplacements(TeamType teamType, PositionType positionType);

    void substitutePlayer(TeamType teamType, int number, PositionType positionType);

    void confirmStartingLineup();

    boolean isStartingLineupConfirmed();
}
