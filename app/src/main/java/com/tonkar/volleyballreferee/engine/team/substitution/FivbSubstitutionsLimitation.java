package com.tonkar.volleyballreferee.engine.team.substitution;

import com.tonkar.volleyballreferee.engine.api.model.SubstitutionDto;

import java.util.*;

public class FivbSubstitutionsLimitation extends SubstitutionsLimitation {

    public FivbSubstitutionsLimitation() {
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
        // A player can only do one return trip in each set
        int count = 0;

        for (SubstitutionDto substitution : substitutions) {
            if (substitution.getPlayerIn() == number || substitution.getPlayerOut() == number) {
                count++;
            }
        }

        return count < 2;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<SubstitutionDto> substitutions, int number, List<Integer> freePlayersOnBench) {
        Set<Integer> substituteNumbers = new HashSet<>();

        for (SubstitutionDto substitution : substitutions) {
            if (substitution.getPlayerIn() == number) {
                substituteNumbers.add(substitution.getPlayerOut());
            } else if (substitution.getPlayerOut() == number) {
                substituteNumbers.add(substitution.getPlayerIn());
            }
        }

        return substituteNumbers;
    }
}
