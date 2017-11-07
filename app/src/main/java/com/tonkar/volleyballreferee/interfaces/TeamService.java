package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface TeamService extends BaseTeamService {

    void addTeamListener(TeamListener listener);

    void removeTeamListener(final TeamListener listener);

    void setTeamName(TeamType teamType, String name);

    void setTeamColor(TeamType teamType, int colorId);

    void addPlayer(TeamType teamType, int number);

    void removePlayer(TeamType teamType, int number);

    boolean hasPlayer(TeamType teamType, int number);

    int getNumberOfPlayers(TeamType teamType);

    List<Integer> getPlayersOnBench(TeamType teamType);

    List<Integer> getPlayersOnCourt(TeamType teamType);

    PositionType getPlayerPosition(TeamType teamType, int number);

    void substitutePlayer(TeamType teamType, int number, PositionType positionType);

    void swapTeams(ActionOriginType actionOriginType);

    TeamType getTeamOnLeftSide();

    TeamType getTeamOnRightSide();

}
