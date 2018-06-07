package com.tonkar.volleyballreferee.interfaces.team;

import com.tonkar.volleyballreferee.interfaces.GameType;

import java.util.List;
import java.util.Set;

public interface BaseTeamService {

    GameType getTeamsKind();

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

    int getLiberoColor(TeamType teamType);

    void setLiberoColor(TeamType teamType, int color);

    void addLibero(TeamType teamType, int number);

    void removeLibero(TeamType teamType, int number);

    boolean isLibero(TeamType teamType, int number);

    boolean canAddLibero(TeamType teamType);

    Set<Integer> getLiberos(TeamType teamType);

    List<Substitution> getSubstitutions(TeamType teamType);

    List<Substitution> getSubstitutions(TeamType teamType, int setIndex);

    boolean isStartingLineupConfirmed();

    Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex);

    PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex);

    int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex);

    void setCaptain(TeamType teamType, int number);

    int getCaptain(TeamType teamType);

    Set<Integer> getPossibleCaptains(TeamType teamType);

    boolean isCaptain(TeamType teamType, int number);

}
