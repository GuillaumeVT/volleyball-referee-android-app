package com.tonkar.volleyballreferee.engine.api.model;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import java.util.*;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class TeamDto {

    public static final String DEFAULT_COLOR = "#633303";

    @SerializedName("id")
    private String          id;
    @SerializedName("createdBy")
    private String          createdBy;
    @SerializedName("createdAt")
    private long            createdAt;
    @SerializedName("updatedAt")
    private long            updatedAt;
    @SerializedName("name")
    private String          name;
    @SerializedName("kind")
    private GameType        kind;
    @SerializedName("gender")
    private GenderType      gender;
    @SerializedName("color")
    private String          color;
    @SerializedName("liberoColor")
    private String          liberoColor;
    @SerializedName("players")
    private List<PlayerDto> players;
    @SerializedName("liberos")
    private List<PlayerDto> liberos;
    @SerializedName("captain")
    private int             captain;
    @SerializedName("coach")
    private String          coach;

    public TeamDto() {
        id = UUID.randomUUID().toString();
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        kind = GameType.INDOOR;
        gender = GenderType.MIXED;
        name = "";
        color = DEFAULT_COLOR;
        liberoColor = DEFAULT_COLOR;
        players = new ArrayList<>();
        liberos = new ArrayList<>();
        captain = -1;
        coach = "";
    }

    public int getColorInt() {
        return Color.parseColor(color);
    }

    public void setColorInt(int colorInt) {
        color = colorIntToHtml(colorInt);
    }

    public int getLiberoColorInt() {
        return Color.parseColor(liberoColor);
    }

    public void setLiberoColorInt(int liberoColorInt) {
        liberoColor = colorIntToHtml(liberoColorInt);
    }

    private String colorIntToHtml(int color) {
        return String.format("#%06X", (0xFFFFFF & color)).toLowerCase();
    }

}
