package com.tonkar.volleyballreferee.engine.team.substitution;

import com.tonkar.volleyballreferee.engine.api.model.SubstitutionDto;

import java.util.*;

public class NoSubstitutionsLimitation extends SubstitutionsLimitation {

    public NoSubstitutionsLimitation() {
        super();
    }

    @Override
    public boolean isInvolvedInPastSubstitution(List<SubstitutionDto> substitutions, int number) {
        return false;
    }

    @Override
    public boolean canSubstitute(List<SubstitutionDto> substitutions, int number) {
        return true;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<SubstitutionDto> substitutions, int number, List<Integer> freePlayersOnBench) {
        return new HashSet<>();
    }
}
