package com.tonkar.volleyballreferee.engine.team.substitution;

import com.tonkar.volleyballreferee.engine.api.model.ApiSubstitution;

import java.util.*;

public class NoSubstitutionsLimitation extends SubstitutionsLimitation {

    public NoSubstitutionsLimitation() {
        super();
    }

    @Override
    public boolean isInvolvedInPastSubstitution(List<ApiSubstitution> substitutions, int number) {
        return false;
    }

    @Override
    public boolean canSubstitute(List<ApiSubstitution> substitutions, int number) {
        return true;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<ApiSubstitution> substitutions, int number, List<Integer> freePlayersOnBench) {
        return new HashSet<>();
    }
}
