package com.tonkar.volleyballreferee.api;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @EqualsAndHashCode
public class ApiTeam {

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
    private List<ApiPlayer> players;
    @SerializedName("liberos")
    private List<ApiPlayer> liberos;
    @SerializedName("captain")
    private int             captain;

    public ApiTeam() {
        id = UUID.randomUUID().toString();
        createdBy = Authentication.VBR_USER_ID;
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        kind = GameType.INDOOR;
        gender = GenderType.MIXED;
        name = "";
        color = DEFAULT_COLOR;
        liberoColor = DEFAULT_COLOR;
        players = new ArrayList<>();
        liberos = new ArrayList<>();
        captain = -1;
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
