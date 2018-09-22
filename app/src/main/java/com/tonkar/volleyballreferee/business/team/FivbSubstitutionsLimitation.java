package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.SubstitutionsLimitation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FivbSubstitutionsLimitation extends SubstitutionsLimitation {

    FivbSubstitutionsLimitation() {
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
        // A player can only do one return trip in each set
        int count = 0;

        for (Substitution substitution : substitutions) {
            if (substitution.getPlayerIn() == number || substitution.getPlayerOut() == number) {
                count++;
            }
        }

        return count < 2;
    }

    @Override
    public Set<Integer> getSubstitutePlayers(List<Substitution> substitutions, int number, List<Integer> freePlayersOnBench) {
        Set<Integer> substituteNumbers = new HashSet<>();

        for (Substitution substitution : substitutions) {
            if (substitution.getPlayerIn() == number) {
                substituteNumbers.add(substitution.getPlayerOut());
            } else if (substitution.getPlayerOut() == number) {
                substituteNumbers.add(substitution.getPlayerIn());
            }
        }

        return substituteNumbers;
    }
}
