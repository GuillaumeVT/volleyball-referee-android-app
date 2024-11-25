package com.tonkar.volleyballreferee.engine.team.substitution;

import com.tonkar.volleyballreferee.engine.api.model.SubstitutionDto;

import java.util.*;

public class AlternativeSubstitutionsLimitation2 extends SubstitutionsLimitation {

    public AlternativeSubstitutionsLimitation2() {
        super();
    }

    @Override
    public boolean isInvolvedInPastSubstitution(List<SubstitutionDto> substitutions, int number) {
        boolean involved = false;

        for (SubstitutionDto substitution : substitutions) {
            if (substitution.getPlayerIn() == number || substitution.getPlayerOut() == number) {
                involved = true;
                break;
            }
        }

        return involved;
    }

    @Override
    public boolean canSubstitute(List<SubstitutionDto> substitutions, int number) {
        return true;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<SubstitutionDto> substitutions, int number, List<Integer> freePlayersOnBench) {
        Set<Integer> substituteNumbers = new HashSet<>();
        substituteNumbers.add(number);
        findSubstitutePlayers(substitutions, substituteNumbers, 2);
        substituteNumbers.remove(number);
        substituteNumbers.addAll(freePlayersOnBench);
        return substituteNumbers;
    }

    private void findSubstitutePlayers(List<SubstitutionDto> substitutions, Set<Integer> substituteNumbers, int numberOfRecursions) {
        for (SubstitutionDto substitution : substitutions) {
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
