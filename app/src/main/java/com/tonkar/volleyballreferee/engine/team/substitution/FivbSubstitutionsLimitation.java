package com.tonkar.volleyballreferee.engine.team.substitution;

import com.tonkar.volleyballreferee.engine.api.model.ApiSubstitution;

import java.util.*;

public class FivbSubstitutionsLimitation extends SubstitutionsLimitation {

    public FivbSubstitutionsLimitation() {
        super();
    }

    @Override
    public boolean isInvolvedInPastSubstitution(List<ApiSubstitution> substitutions, int number) {
        boolean involved = false;

        for (ApiSubstitution substitution : substitutions) {
            if (substitution.getPlayerIn() == number || substitution.getPlayerOut() == number) {
                involved = true;
                break;
            }
        }

        return involved;
    }

    @Override
    public boolean canSubstitute(List<ApiSubstitution> substitutions, int number) {
        // A player can only do one return trip in each set
        int count = 0;

        for (ApiSubstitution substitution : substitutions) {
            if (substitution.getPlayerIn() == number || substitution.getPlayerOut() == number) {
                count++;
            }
        }

        return count < 2;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<ApiSubstitution> substitutions, int number, List<Integer> freePlayersOnBench) {
        Set<Integer> substituteNumbers = new HashSet<>();

        for (ApiSubstitution substitution : substitutions) {
            if (substitution.getPlayerIn() == number) {
                substituteNumbers.add(substitution.getPlayerOut());
            } else if (substitution.getPlayerOut() == number) {
                substituteNumbers.add(substitution.getPlayerIn());
            }
        }

        return substituteNumbers;
    }
}
