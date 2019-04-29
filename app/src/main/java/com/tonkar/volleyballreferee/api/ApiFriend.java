package com.tonkar.volleyballreferee.api;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ApiFriend {

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

    public ApiFriend(String id, String pseudo) {
        this.id = id;
        this.pseudo = pseudo;
    }

}
