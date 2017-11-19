package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface TeamService extends BaseTeamService {

    void addTeamListener(TeamListener listener);

    void removeTeamListener(final TeamListener listener);

    void setTeamName(TeamType teamType, String name);

    void setTeamColor(TeamType teamType, int color);

    void addPlayer(TeamType teamType, int number);

    void removePlayer(TeamType teamType, int number);

    boolean hasPlayer(TeamType teamType, int number);

    List<Integer> getPlayers(TeamType teamType);

    List<Integer> getPlayersOnCourt(TeamType teamType);

    PositionType getPlayerPosition(TeamType teamType, int number);

    int getPlayerAtPosition(TeamType teamType, PositionType positionType);

    void swapTeams(ActionOriginType actionOriginType);

    TeamType getTeamOnLeftSide();

    TeamType getTeamOnRightSide();

}
