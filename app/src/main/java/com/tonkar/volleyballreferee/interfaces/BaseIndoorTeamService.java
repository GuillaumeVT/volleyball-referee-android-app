package com.tonkar.volleyballreferee.interfaces;

import java.util.List;
import java.util.Set;

public interface BaseIndoorTeamService extends BaseTeamService {

    int getLiberoColor(TeamType teamType);

    void setLiberoColor(TeamType teamType, int color);

    void addLibero(TeamType teamType, int number);

    void removeLibero(TeamType teamType, int number);

    boolean isLibero(TeamType teamType, int number);

    boolean canAddLibero(TeamType teamType);

    List<Substitution> getSubstitutions(TeamType teamType);

    List<Substitution> getSubstitutions(TeamType teamType, int setIndex);

    boolean isStartingLineupConfirmed();

    Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex);

    PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex);

    int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex);

}
