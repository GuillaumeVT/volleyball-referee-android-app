package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.*;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class LeagueDto {
    @SerializedName("id")
    private String id;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("createdAt")
    private long createdAt;

    @SerializedName("updatedAt")
    private long updatedAt;

    @SerializedName("name")
    private String name;

    @SerializedName("kind")
    private GameType kind;

    @SerializedName("divisions")
    private List<String> divisions;

    public LeagueDto() {
        id = UUID.randomUUID().toString();
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        kind = GameType.INDOOR;
        name = "";
        divisions = new ArrayList<>();
    }

    public void setAll(SelectedLeagueDto league) {
        if (league != null) {
            setId(league.getId());
            setCreatedBy(league.getCreatedBy());
            setCreatedAt(league.getCreatedAt());
            setUpdatedAt(league.getUpdatedAt());
            setKind(league.getKind());
            setName(league.getName());
            getDivisions().add(league.getDivision());
        }
    }
}
