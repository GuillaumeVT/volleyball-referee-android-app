package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class TimeoutDto {
    @SerializedName("homePoints")
    private int homePoints;
    @SerializedName("guestPoints")
    private int guestPoints;
}