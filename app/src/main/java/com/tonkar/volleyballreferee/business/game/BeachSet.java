package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.BeachTeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

public class BeachSet extends Set {

    public BeachSet(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart, TeamDefinition homeTeamDefinition, TeamDefinition guestTeamDefinition) {
        super(rules, pointsToWinSet, servingTeamAtStart, homeTeamDefinition, guestTeamDefinition);
    }

    // For GSON Deserialization
    public BeachSet() {
        super(Rules.OFFICIAL_BEACH_RULES, 0, TeamType.HOME);
    }

    @Override
    protected TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition) {
        return new BeachTeamComposition(teamDefinition);
    }

}
