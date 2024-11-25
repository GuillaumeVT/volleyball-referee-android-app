package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
public class CourtDto {
    @SerializedName("p1")
    private int p1;
    @SerializedName("p2")
    private int p2;
    @SerializedName("p3")
    private int p3;
    @SerializedName("p4")
    private int p4;
    @SerializedName("p5")
    private int p5;
    @SerializedName("p6")
    private int p6;

    public CourtDto() {
        this.p1 = -1;
        this.p2 = -1;
        this.p3 = -1;
        this.p4 = -1;
        this.p5 = -1;
        this.p6 = -1;
    }

    public boolean isFilled(GameType kind) {
        return switch (kind) {
            case INDOOR -> p1 >= 0 && p2 >= 0 && p3 >= 0 && p4 >= 0 && p5 >= 0 && p6 >= 0;
            case INDOOR_4X4 -> p1 >= 0 && p2 >= 0 && p3 >= 0 && p4 >= 0;
            case SNOW -> p1 >= 0 && p2 >= 0 && p3 >= 0;
            default -> true;
        };
    }

    public PositionType getPositionOf(int number) {
        PositionType positionType;

        if (number == p1) {
            positionType = PositionType.POSITION_1;
        } else if (number == p2) {
            positionType = PositionType.POSITION_2;
        } else if (number == p3) {
            positionType = PositionType.POSITION_3;
        } else if (number == p4) {
            positionType = PositionType.POSITION_4;
        } else if (number == p5) {
            positionType = PositionType.POSITION_5;
        } else if (number == p6) {
            positionType = PositionType.POSITION_6;
        } else {
            positionType = PositionType.BENCH;
        }

        return positionType;
    }

    public int getPlayerAt(PositionType positionType) {
        return switch (positionType) {
            case POSITION_1 -> p1;
            case POSITION_2 -> p2;
            case POSITION_3 -> p3;
            case POSITION_4 -> p4;
            case POSITION_5 -> p5;
            case POSITION_6 -> p6;
            default -> -1;
        };
    }

    public void setPlayerAt(int number, PositionType positionType) {
        switch (positionType) {
            case POSITION_1 -> setP1(number);
            case POSITION_2 -> setP2(number);
            case POSITION_3 -> setP3(number);
            case POSITION_4 -> setP4(number);
            case POSITION_5 -> setP5(number);
            case POSITION_6 -> setP6(number);
            default -> {
            }
        }
    }
}
