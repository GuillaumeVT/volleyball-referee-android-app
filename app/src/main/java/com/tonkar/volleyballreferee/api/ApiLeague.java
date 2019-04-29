package com.tonkar.volleyballreferee.api;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.GameType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @EqualsAndHashCode
public class ApiLeague {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("id")
    private String       id;
    @NonNull
    @ColumnInfo(name = "createdBy")
    @SerializedName("createdBy")
    private String       createdBy;
    @ColumnInfo(name = "createdAt")
    @SerializedName("createdAt")
    private long         createdAt;
    @ColumnInfo(name = "updatedAt")
    @SerializedName("updatedAt")
    private long         updatedAt;
    @ColumnInfo(name = "synced")
    @SerializedName("synced")
    private boolean      synced;
    @NonNull
    @ColumnInfo(name = "name")
    @SerializedName("name")
    private String       name;
    @NonNull
    @ColumnInfo(name = "kind")
    @SerializedName("kind")
    private GameType     kind;
    @Ignore
    @SerializedName("divisions")
    private List<String> divisions;

    public ApiLeague() {
        id = UUID.randomUUID().toString();
        createdBy = Authentication.VBR_USER_ID;
        createdAt = 0L;
        updatedAt = 0L;
        kind = GameType.INDOOR;
        name = "";
        divisions = new ArrayList<>();
    }

}
