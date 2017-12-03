package com.tonkar.volleyballreferee.interfaces;

import java.util.Set;

public interface IndoorTeamService extends TeamService, BaseIndoorTeamService {

    Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType);

    void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType);

    void confirmStartingLineup();

}
