package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class GameDto {
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
    @SerializedName("league")
    private SelectedLeagueDto league;
    @SerializedName("homeTeam")
    private TeamDto           homeTeam;
    @SerializedName("guestTeam")
    private TeamDto           guestTeam;
    @SerializedName("homeSets")
    private int               homeSets;
    @SerializedName("guestSets")
    private int               guestSets;
    @SerializedName("sets")
    private List<SetDto>      sets;
    @SerializedName("homeCards")
    private List<SanctionDto> homeCards;
    @SerializedName("guestCards")
    private List<SanctionDto> guestCards;
    @SerializedName("rules")
    private RulesDto          rules;
    @SerializedName("score")
    private String            score;
    @SerializedName("startTime")
    private long              startTime;
    @SerializedName("endTime")
    private long              endTime;
    @SerializedName("referee1")
    private String            referee1Name;
    @SerializedName("referee2")
    private String            referee2Name;
    @SerializedName("scorer")
    private String            scorerName;

    public GameDto() {
        id = UUID.randomUUID().toString();
        createdBy = null;
        createdAt = 0L;
        updatedAt = 0L;
        scheduledAt = 0L;
        refereedBy = null;
        refereeName = "";
        kind = GameType.INDOOR;
        gender = GenderType.MIXED;
        usage = UsageType.NORMAL;
        status = GameStatus.SCHEDULED;
        league = null;
        homeTeam = null;
        guestTeam = null;
        homeSets = 0;
        guestSets = 0;
        sets = new ArrayList<>();
        homeCards = new ArrayList<>();
        guestCards = new ArrayList<>();
        rules = null;
        score = "";
        startTime = 0L;
        endTime = 0L;
        referee1Name = "";
        referee2Name = "";
        scorerName = "";
    }

    public String buildScore() {
        StringBuilder builder = new StringBuilder();

        for (SetDto set : sets) {
            builder.append(UiUtils.formatScoreFromLocale(set.getHomePoints(), set.getGuestPoints(), false)).append("\t\t");
        }

        return builder.toString().trim();
    }
}
