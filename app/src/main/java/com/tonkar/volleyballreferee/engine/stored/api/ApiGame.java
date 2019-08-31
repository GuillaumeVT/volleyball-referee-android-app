package com.tonkar.volleyballreferee.engine.stored.api;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @EqualsAndHashCode
public class ApiGame {

    @SerializedName("id")
    private String            id;
    @SerializedName("createdBy")
    private String            createdBy;
    @SerializedName("createdAt")
    private long              createdAt;
    @SerializedName("updatedAt")
    private long              updatedAt;
    @SerializedName("scheduledAt")
    private long              scheduledAt;
    @SerializedName("refereedBy")
    private String            refereedBy;
    @SerializedName("refereeName")
    private String            refereeName;
    @SerializedName("kind")
    private GameType          kind;
    @SerializedName("gender")
    private GenderType        gender;
    @SerializedName("usage")
    private UsageType         usage;
    @SerializedName("status")
    private GameStatus        status;
    @SerializedName("indexed")
    private boolean           indexed;
    @SerializedName("league")
    private ApiSelectedLeague league;
    @SerializedName("homeTeam")
    private ApiTeam           homeTeam;
    @SerializedName("guestTeam")
    private ApiTeam           guestTeam;
    @SerializedName("homeSets")
    private int               homeSets;
    @SerializedName("guestSets")
    private int               guestSets;
    @SerializedName("sets")
    private List<ApiSet>      sets;
    @SerializedName("homeCards")
    private List<ApiSanction> homeCards;
    @SerializedName("guestCards")
    private List<ApiSanction> guestCards;
    @SerializedName("rules")
    private ApiRules          rules;
    @SerializedName("score")
    private String            score;

    public ApiGame() {
        id = UUID.randomUUID().toString();
        createdBy = ApiUserSummary.VBR_USER_ID;
        createdAt = 0L;
        updatedAt = 0L;
        scheduledAt = 0L;
        refereedBy = ApiUserSummary.VBR_USER_ID;
        refereeName = "";
        kind = GameType.INDOOR;
        gender = GenderType.MIXED;
        usage = UsageType.NORMAL;
        status = GameStatus.SCHEDULED;
        indexed = true;
        league = null;
        homeTeam = null;
        guestTeam = null;
        homeSets = 0;
        guestSets = 0;
        sets = new ArrayList<>();
        homeCards = new ArrayList<>();
        guestCards = new ArrayList<>();
        rules = null;
    }

}
