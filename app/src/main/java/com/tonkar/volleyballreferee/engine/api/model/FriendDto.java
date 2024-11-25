package com.tonkar.volleyballreferee.engine.api.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class FriendDto {
    @SerializedName("id")
    private String id;

    @SerializedName("pseudo")
    private String pseudo;

    public FriendDto() {
        this("", "");
    }

    public FriendDto(@NonNull String id, @NonNull String pseudo) {
        this.id = id;
        this.pseudo = pseudo;
    }

}
