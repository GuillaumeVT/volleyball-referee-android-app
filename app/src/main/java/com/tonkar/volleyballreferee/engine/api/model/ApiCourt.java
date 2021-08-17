package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ApiCourt {
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

    public ApiCourt() {
        this.p1 = -1;
        this.p2 = -1;
        this.p3 = -1;
        this.p4 = -1;
        this.p5 = -1;
        this.p6 = -1;
    }

    public boolean isFilled(GameType kind) {
        switch (kind) {
            case INDOOR:
                return p1 >= 0 && p2 >= 0 && p3 >= 0 && p4 >= 0 && p5 >= 0 && p6 >= 0;
            case INDOOR_4X4:
                return p1 >= 0 && p2 >= 0 && p3 >= 0 && p4 >= 0;
            case SNOW:
                return p1 >= 0 && p2 >= 0 && p3 >= 0;
            case BEACH:
            case TIME:
            default:
                return true;
        }
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
        int number;

        switch (positionType) {
            case POSITION_1:
                number = p1;
                break;
            case POSITION_2:
                number = p2;
                break;
            case POSITION_3:
                number = p3;
                break;
            case POSITION_4:
                number = p4;
                break;
            case POSITION_5:
                number = p5;
                break;
            case POSITION_6:
                number = p6;
                break;
            default:
                number = -1;
                break;
        }

        return number;
    }

    public void setPlayerAt(int number, PositionType positionType) {
        switch (positionType) {
            case POSITION_1:
                setP1(number);
                break;
            case POSITION_2:
                setP2(number);
                break;
            case POSITION_3:
                setP3(number);
                break;
            case POSITION_4:
                setP4(number);
                break;
            case POSITION_5:
                setP5(number);
                break;
            case POSITION_6:
                setP6(number);
                break;
            default:
                break;
        }
    }
}
