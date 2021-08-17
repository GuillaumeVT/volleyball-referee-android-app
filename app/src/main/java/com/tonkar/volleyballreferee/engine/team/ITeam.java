package com.tonkar.volleyballreferee.engine.team;

import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.Set;

public interface ITeam extends IBaseTeam {

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

    void restoreTeams(IStoredGame storedGame);

}
