package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@Getter
@Setter
public class SelectedLeagueDto extends LeagueSummaryDto {

    @SerializedName("division")
    private String division;

    public SelectedLeagueDto() {
        super();
        division = "";
    }

    public void setAll(SelectedLeagueDto league) {
        super.setAll(league);
        if (league != null) {
            setDivision(league.getDivision());
        }
    }
}
