package com.tonkar.volleyballreferee.engine.game.set;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;

public class Indoor4x4Set extends Set {

    public Indoor4x4Set(Rules rules,
                        int pointsToWinSet,
                        TeamType servingTeamAtStart,
                        TeamDefinition homeTeamDefinition,
                        TeamDefinition guestTeamDefinition) {
        super(rules, pointsToWinSet, servingTeamAtStart, homeTeamDefinition, guestTeamDefinition);
    }

    // For GSON Deserialization
    public Indoor4x4Set() {
        super(Rules.defaultIndoor4x4Rules(), 0, TeamType.HOME);
    }

    @Override
    protected TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition) {
        return new Indoor4x4TeamComposition(teamDefinition, rules.getSubstitutionsLimitation(), rules.getTeamSubstitutionsPerSet());
    }

}
