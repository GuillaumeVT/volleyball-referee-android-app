package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SanctionDto {
    @SerializedName("card")
    private SanctionType card;
    @SerializedName("num")
    private int          num; // 0-99 player, 100 coach, 200 team
    @SerializedName("set")
    private int          set;
    @SerializedName("homePoints")
    private int          homePoints;
    @SerializedName("guestPoints")
    private int          guestPoints;

    public boolean isPlayer() {
        return num >= 0 && num < COACH;
    }

    public boolean isCoach() {
        return num == COACH;
    }

    public boolean isTeam() {
        return num == TEAM;
    }

    public static boolean isPlayer(int num) {
        return num >= 0 && num < COACH;
    }

    public static boolean isCoach(int num) {
        return num == COACH;
    }

    public static boolean isTeam(int num) {
        return num == TEAM;
    }

    public static final int COACH = 100;
    public static final int TEAM  = 200;
}


