package com.tonkar.volleyballreferee.engine.team.substitution;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSubstitution;

import java.util.List;
import java.util.Set;

public abstract class SubstitutionsLimitation {

    @SerializedName("classType")
    private String mClassType;

    public SubstitutionsLimitation() {
        mClassType = getClass().getName();
    }

    public abstract boolean isInvolvedInPastSubstitution(List<ApiSubstitution> substitutions, int number);

    public abstract boolean canSubstitute(List<ApiSubstitution> substitutions, int number);

    public abstract Set<Integer> getSubstitutePlayers(List<ApiSubstitution> substitutions, int number, List<Integer> freePlayersOnBench);

}
