package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SubstitutionDto {
    @SerializedName("playerIn")
    private int playerIn;
    @SerializedName("playerOut")
    private int playerOut;
    @SerializedName("homePoints")
    private int homePoints;
    @SerializedName("guestPoints")
    private int guestPoints;
}
