package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@Getter
@Setter
public class ApiSelectedLeague extends ApiLeagueSummary {
    @SerializedName("division")
    private String division;

    public ApiSelectedLeague() {
        super();
        division = "";
    }

    public void setAll(ApiSelectedLeague league) {
        super.setAll(league);
        if (league != null) {
            setDivision(league.getDivision());
        }
    }
}
