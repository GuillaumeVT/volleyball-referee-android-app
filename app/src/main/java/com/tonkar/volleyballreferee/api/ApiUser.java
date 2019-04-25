package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor @Getter @Setter
public class ApiUser {

    @SerializedName("id")
    private String       id;
    @SerializedName("pseudo")
    private String       pseudo;
    @SerializedName("friends")
    private List<Friend> friends;

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    public static class Friend {

        @SerializedName("id")
        private String id;
        @SerializedName("pseudo")
        private String pseudo;

    }

}

