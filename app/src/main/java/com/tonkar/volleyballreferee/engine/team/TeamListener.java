package com.tonkar.volleyballreferee.engine.team;

import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

public interface TeamListener {

    void onStartingLineupSubmitted(TeamType teamType);

    void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType);

    void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType);

    void onTeamRotated(TeamType teamType, boolean clockwise);
}
