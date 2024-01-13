package com.tonkar.volleyballreferee.engine.api.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class ApiFriend {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("id")
    private String id;
    @NonNull
    @ColumnInfo(name = "pseudo")
    @SerializedName("pseudo")
    private String pseudo;

    public ApiFriend() {
        this("", "");
    }

    public ApiFriend(@NonNull String id, @NonNull String pseudo) {
        this.id = id;
        this.pseudo = pseudo;
    }

}
