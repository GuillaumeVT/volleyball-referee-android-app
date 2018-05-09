package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.Indoor4x4TeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

public class Indoor4x4Set extends Set {

    public Indoor4x4Set(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart, TeamDefinition homeTeamDefinition, TeamDefinition guestTeamDefinition) {
        super(rules, pointsToWinSet, servingTeamAtStart, homeTeamDefinition, guestTeamDefinition);
    }

    // For GSON Deserialization
    public Indoor4x4Set() {
        super(Rules.DEFAULT_INDOOR_4X4_RULES, 0, TeamType.HOME);
    }

    @Override
    protected TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition) {
        return new Indoor4x4TeamComposition(teamDefinition, rules.getTeamSubstitutionsPerSet());
    }


}
