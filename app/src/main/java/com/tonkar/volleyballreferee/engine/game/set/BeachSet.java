package com.tonkar.volleyballreferee.engine.game.set;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;

public class BeachSet extends Set {

    public BeachSet(Rules rules,
                    int pointsToWinSet,
                    TeamType servingTeamAtStart,
                    TeamDefinition homeTeamDefinition,
                    TeamDefinition guestTeamDefinition) {
        super(rules, pointsToWinSet, servingTeamAtStart, homeTeamDefinition, guestTeamDefinition);
    }

    // For GSON Deserialization
    public BeachSet() {
        super(Rules.officialBeachRules(), 0, TeamType.HOME);
    }

    @Override
    protected TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition) {
        return new BeachTeamComposition(teamDefinition);
    }

}
