package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.SubstitutionService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluralSubstitutionService extends SubstitutionService {

    PluralSubstitutionService() {
        super();
    }

    @Override
    public boolean isInvolvedInPastSubstitution(List<Substitution> substitutions, int number) {
        boolean involved = false;

        for (Substitution substitution : substitutions) {
            if (substitution.getPlayerIn() == number || substitution.getPlayerOut() == number) {
                involved = true;
            }
        }

        return involved;
    }

    @Override
    public boolean canSubstitute(List<Substitution> substitutions, int number) {
        return true;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<Substitution> substitutions, int number, List<Integer> freePlayersOnBench) {
        Set<Integer> substituteNumbers = new HashSet<>();
        substituteNumbers.add(number);
        findSubstitutePlayers(substitutions, substituteNumbers, 2);
        substituteNumbers.remove(number);
        substituteNumbers.addAll(freePlayersOnBench);
        return substituteNumbers;
    }

    private void findSubstitutePlayers(List<Substitution> substitutions, Set<Integer> substituteNumbers, int numberOfRecursions) {
        for (Substitution substitution : substitutions) {
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
