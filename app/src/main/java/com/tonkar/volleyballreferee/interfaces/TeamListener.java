package com.tonkar.volleyballreferee.interfaces;

public interface TeamListener {

    void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType);

    void onPlayerChanged(TeamType teamType, int number, PositionType positionType);

    void onTeamRotated(TeamType teamType);
}
