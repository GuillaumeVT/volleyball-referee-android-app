package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public class BeachTeamDefinition extends TeamDefinition {

    public BeachTeamDefinition(final TeamType teamType) {
        super(teamType);

        addPlayer(1);
        addPlayer(2);
    }

    // For GSON Deserialization
    public BeachTeamDefinition() {
        this(TeamType.HOME);
    }
}
