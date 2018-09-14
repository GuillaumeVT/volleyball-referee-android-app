package com.tonkar.volleyballreferee.interfaces.team;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Set;

public abstract class SubstitutionService {

    @SerializedName("classType")
    private String mClassType;

    public SubstitutionService() {
        mClassType = getClass().getName();
    }

    public abstract boolean isInvolvedInPastSubstitution(List<Substitution> substitutions, int number);

    public abstract boolean canSubstitute(List<Substitution> substitutions, int number);

    public abstract Set<Integer> getSubstitutePlayers(List<Substitution> substitutions, int number, List<Integer> freePlayersOnBench);

    private String getClassType() {
        return mClassType;
    }

}
