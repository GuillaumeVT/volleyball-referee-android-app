package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public class EmptyTeamDefinition extends TeamDefinition {

    public EmptyTeamDefinition(final TeamType teamType) {
        super(teamType);
    }

    // For GSON Deserialization
    public EmptyTeamDefinition() {
        this(TeamType.HOME);
    }
}
