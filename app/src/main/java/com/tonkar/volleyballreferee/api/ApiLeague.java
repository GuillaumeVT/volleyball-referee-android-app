package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @EqualsAndHashCode
public class ApiLeague {

    @SerializedName("id")
    private String       id;
    @SerializedName("createdBy")
    private String       createdBy;
    @SerializedName("createdAt")
    private long         createdAt;
    @SerializedName("updatedAt")
    private long         updatedAt;
    @SerializedName("name")
    private String       name;
    @SerializedName("kind")
    private GameType     kind;
    @SerializedName("divisions")
    private List<String> divisions;

    public ApiLeague() {
        id = UUID.randomUUID().toString();
        createdBy = Authentication.VBR_USER_ID;
        createdAt = 0L;
        updatedAt = 0L;
        kind = GameType.INDOOR;
        name = "";
        divisions = new ArrayList<>();
    }

    public void setAll(ApiSelectedLeague league) {
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
