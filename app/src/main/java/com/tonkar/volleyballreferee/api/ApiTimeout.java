package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter @EqualsAndHashCode
public class ApiTimeout {

    @SerializedName("homePoints")
    private int homePoints;
    @SerializedName("guestPoints")
    private int guestPoints;

}