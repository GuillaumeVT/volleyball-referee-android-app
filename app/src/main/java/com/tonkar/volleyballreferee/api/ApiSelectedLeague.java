package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ApiSelectedLeague extends ApiLeagueDescription {

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
