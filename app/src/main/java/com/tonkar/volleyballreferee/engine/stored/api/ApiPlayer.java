package com.tonkar.volleyballreferee.engine.stored.api;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class ApiPlayer implements Comparable<ApiPlayer> {

    @SerializedName("num")
    private int    num;
    @SerializedName("name")
    private String name;

    public ApiPlayer(int num) {
        this(num, "");
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof ApiPlayer) {
            ApiPlayer other = (ApiPlayer) obj;
            result = num == other.num;
        }

        return result;
    }

    @Override
    public int compareTo(ApiPlayer other) {
        return Integer.compare(num, other.num);
    }

}
