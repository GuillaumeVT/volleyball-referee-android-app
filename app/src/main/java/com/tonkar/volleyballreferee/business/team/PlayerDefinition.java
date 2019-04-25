package com.tonkar.volleyballreferee.business.team;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class PlayerDefinition {

    @SerializedName("num")
    private int    num;
    @SerializedName("name")
    private String name;

}
