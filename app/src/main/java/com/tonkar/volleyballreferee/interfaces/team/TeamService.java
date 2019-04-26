package com.tonkar.volleyballreferee.interfaces.team;

import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;

import java.util.Set;

public interface TeamService extends BaseTeamService {

    void addTeamListener(TeamListener listener);

    void removeTeamListener(final TeamListener listener);

    Set<Integer> getPlayersOnCourt(TeamType teamType);

    Set<Integer> getPlayersOnCourt(TeamType teamType, int setIndex);

    PositionType getPlayerPosition(TeamType teamType, int number);

    PositionType getPlayerPosition(TeamType teamType, int number, int setIndex);

    int getPlayerAtPosition(TeamType teamType, PositionType positionType);

    int getPlayerAtPosition(TeamType teamType, PositionType positionType, int setIndex);

    void swapTeams(ActionOriginType actionOriginType);

    TeamType getTeamOnLeftSide();

    TeamType getTeamOnRightSide();

    void restoreTeams(StoredGameService storedGameService);

}
