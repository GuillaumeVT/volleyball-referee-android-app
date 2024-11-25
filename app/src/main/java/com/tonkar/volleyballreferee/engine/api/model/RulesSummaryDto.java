package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class RulesSummaryDto {
    @SerializedName("id")
    private String id;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("createdAt")
    private long createdAt;

    @SerializedName("updatedAt")
    private long updatedAt;

    @SerializedName("synced")
    private boolean synced;

    @SerializedName("name")
    private String name;

    @SerializedName("kind")
    private GameType kind;

    public RulesSummaryDto() {
        id = "";
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        synced = false;
        kind = GameType.INDOOR;
        name = "";
    }

}
