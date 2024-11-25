package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlayerDto implements Comparable<PlayerDto> {
    @SerializedName("num")
    private int    num;
    @SerializedName("name")
    private String name;

    public PlayerDto(int num) {
        this(num, "");
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof PlayerDto other) {
            result = num == other.num;
        }

        return result;
    }

    @Override
    public int compareTo(PlayerDto other) {
        return Integer.compare(num, other.num);
    }

}
