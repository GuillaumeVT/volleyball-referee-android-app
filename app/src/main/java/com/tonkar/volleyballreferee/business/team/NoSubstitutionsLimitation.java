package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.SubstitutionsLimitation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoSubstitutionsLimitation extends SubstitutionsLimitation {

    NoSubstitutionsLimitation() {
        super();
    }

    @Override
    public boolean isInvolvedInPastSubstitution(List<Substitution> substitutions, int number) {
        return false;
    }

    @Override
    public boolean canSubstitute(List<Substitution> substitutions, int number) {
        return true;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<Substitution> substitutions, int number, List<Integer> freePlayersOnBench) {
        return new HashSet<>();
    }
}