package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.api.ApiSubstitution;
import com.tonkar.volleyballreferee.interfaces.team.SubstitutionsLimitation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FivbSubstitutionsLimitation extends SubstitutionsLimitation {

    FivbSubstitutionsLimitation() {
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
