package com.tonkar.volleyballreferee.interfaces.team;

import com.tonkar.volleyballreferee.interfaces.ActionOriginType;

public interface TeamListener {

    void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType);

    void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType);

    void onTeamRotated(TeamType teamType);
}
