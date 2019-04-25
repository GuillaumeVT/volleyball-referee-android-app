package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter @EqualsAndHashCode
public class ApiCourt {

    @SerializedName("p1")
    private int p1;
    @SerializedName("p2")
    private int p2;
    @SerializedName("p3")
    private int p3;
    @SerializedName("p4")
    private int p4;
    @SerializedName("p5")
    private int p5;
    @SerializedName("p6")
    private int p6;

}
