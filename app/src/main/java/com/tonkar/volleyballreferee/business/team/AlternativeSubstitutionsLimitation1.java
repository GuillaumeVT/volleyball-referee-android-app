package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.api.ApiSubstitution;
import com.tonkar.volleyballreferee.interfaces.team.SubstitutionsLimitation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlternativeSubstitutionsLimitation1 extends SubstitutionsLimitation {

    AlternativeSubstitutionsLimitation1() {
        super();
    }

    @Override
    public boolean isInvolvedInPastSubstitution(List<ApiSubstitution> substitutions, int number) {
        boolean involved = false;

        for (ApiSubstitution substitution : substitutions) {
            if (substitution.getPlayerIn() == number || substitution.getPlayerOut() == number) {
                involved = true;
            }
        }

        return involved;
    }

    @Override
    public boolean canSubstitute(List<ApiSubstitution> substitutions, int number) {
        return true;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<ApiSubstitution> substitutions, int number, List<Integer> freePlayersOnBench) {
        Set<Integer> substituteNumbers = new HashSet<>();
        substituteNumbers.add(number);
        findSubstitutePlayers(substitutions, substituteNumbers, 2);
        substituteNumbers.remove(number);
        return substituteNumbers;
    }

    private void findSubstitutePlayers(List<ApiSubstitution> substitutions, Set<Integer> substituteNumbers, int numberOfRecursions) {
        for (ApiSubstitution substitution : substitutions) {
            if (substituteNumbers.contains(substitution.getPlayerIn())) {
                substituteNumbers.add(substitution.getPlayerOut());
            } else if (substituteNumbers.contains(substitution.getPlayerOut())) {
                substituteNumbers.add(substitution.getPlayerIn());
            }
        }

        if (numberOfRecursions > 0) {
            findSubstitutePlayers(substitutions, substituteNumbers, numberOfRecursions - 1);
        }
    }
}
