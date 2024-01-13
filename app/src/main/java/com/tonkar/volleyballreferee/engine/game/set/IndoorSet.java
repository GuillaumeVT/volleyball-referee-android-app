package com.tonkar.volleyballreferee.engine.game.set;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;

public class IndoorSet extends Set {

    public IndoorSet(Rules rules,
                     int pointsToWinSet,
                     TeamType servingTeamAtStart,
                     TeamDefinition homeTeamDefinition,
                     TeamDefinition guestTeamDefinition) {
        super(rules, pointsToWinSet, servingTeamAtStart, homeTeamDefinition, guestTeamDefinition);
    }

    // For GSON Deserialization
    public IndoorSet() {
        super(Rules.officialIndoorRules(), 0, TeamType.HOME);
    }

    @Override
    protected TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition) {
        return new IndoorTeamComposition(teamDefinition, rules.getSubstitutionsLimitation(), rules.getTeamSubstitutionsPerSet());
    }

}
