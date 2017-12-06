package com.tonkar.volleyballreferee.interfaces;

import java.util.Set;

public interface IndoorTeamService extends TeamService, BaseIndoorTeamService {

    Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType);

    void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType);

    void confirmStartingLineup();

    boolean hasActingCaptainOnCourt(TeamType teamType);

    int getActingCaptain(TeamType teamType, int setIndex);

    void setActingCaptain(TeamType teamType, int number);

    boolean isActingCaptain(TeamType teamType, int number);

    Set<Integer> getPossibleActingCaptains(TeamType teamType);

}
