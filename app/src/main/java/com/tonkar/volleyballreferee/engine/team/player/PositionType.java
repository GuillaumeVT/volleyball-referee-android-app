package com.tonkar.volleyballreferee.engine.team.player;

import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.ArrayList;
import java.util.List;

public enum PositionType {

    POSITION_1, POSITION_2, POSITION_3, POSITION_4, POSITION_5, POSITION_6, BENCH;

    public PositionType previousPosition(GameType gameType) {
        PositionType previous = BENCH;

        if (GameType.INDOOR.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    previous = POSITION_2;
                    break;
                case POSITION_2:
                    previous = POSITION_3;
                    break;
                case POSITION_3:
                    previous = POSITION_4;
                    break;
                case POSITION_4:
                    previous = POSITION_5;
                    break;
                case POSITION_5:
                    previous = POSITION_6;
                    break;
                case POSITION_6:
                    previous = POSITION_1;
                    break;
                default:
                    break;
            }
        } else if (GameType.BEACH.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    previous = POSITION_2;
                    break;
                case POSITION_2:
                    previous = POSITION_1;
                    break;
                default:
                    break;
            }
        } else if (GameType.INDOOR_4X4.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    previous = POSITION_2;
                    break;
                case POSITION_2:
                    previous = POSITION_3;
                    break;
                case POSITION_3:
                    previous = POSITION_4;
                    break;
                case POSITION_4:
                    previous = POSITION_1;
                    break;
                default:
                    break;
            }
        } else if (GameType.SNOW.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    previous = POSITION_2;
                    break;
                case POSITION_2:
                    previous = POSITION_3;
                    break;
                case POSITION_3:
                    previous = POSITION_1;
                    break;
                default:
                    break;
            }
        }

        return previous;
    }

    public PositionType nextPosition(GameType gameType) {
        PositionType next = BENCH;

        if (GameType.INDOOR.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    next = POSITION_6;
                    break;
                case POSITION_2:
                    next = POSITION_1;
                    break;
                case POSITION_3:
                    next = POSITION_2;
                    break;
                case POSITION_4:
                    next = POSITION_3;
                    break;
                case POSITION_5:
                    next = POSITION_4;
                    break;
                case POSITION_6:
                    next = POSITION_5;
                    break;
                default:
                    break;
            }
        } else if (GameType.BEACH.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    next = POSITION_2;
                    break;
                case POSITION_2:
                    next = POSITION_1;
                    break;
                default:
                    break;
            }
        } else if (GameType.INDOOR_4X4.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    next = POSITION_4;
                    break;
                case POSITION_2:
                    next = POSITION_1;
                    break;
                case POSITION_3:
                    next = POSITION_2;
                    break;
                case POSITION_4:
                    next = POSITION_3;
                    break;
                default:
                    break;
            }
        } else if (GameType.SNOW.equals(gameType)) {
            switch (this) {
                case POSITION_1:
                    next = POSITION_3;
                    break;
                case POSITION_2:
                    next = POSITION_1;
                    break;
                case POSITION_3:
                    next = POSITION_2;
                    break;
                default:
                    break;
            }
        }

        return next;
    }

    public boolean isAtTheBack() {
        final boolean back;

        switch (this) {
            case POSITION_1:
            case POSITION_5:
            case POSITION_6:
                back = true;
                break;
            case POSITION_2:
            case POSITION_3:
            case POSITION_4:
            case BENCH:
            default:
                back = false;
                break;
        }

        return back;
    }

    public PositionType oppositePosition() {
        final PositionType opposite;

        switch (this) {
            case POSITION_1:
                opposite = POSITION_4;
                break;
            case POSITION_2:
                opposite = POSITION_5;
                break;
            case POSITION_3:
                opposite = POSITION_6;
                break;
            case POSITION_4:
                opposite = POSITION_1;
                break;
            case POSITION_5:
                opposite = POSITION_2;
                break;
            case POSITION_6:
                opposite = POSITION_3;
                break;
            default:
            case BENCH:
                opposite = BENCH;
                break;
        }

        return opposite;
    }

    public static int toInt(PositionType positionType) {
        final int position;

        switch (positionType) {
            case POSITION_1:
                position = 1;
                break;
            case POSITION_2:
                position = 2;
                break;
            case POSITION_3:
                position = 3;
                break;
            case POSITION_4:
                position = 4;
                break;
            case POSITION_5:
                position = 5;
                break;
            case POSITION_6:
                position = 6;
                break;
            default:
            case BENCH:
                position = 0;
                break;
        }

        return position;
    }

    public static PositionType fromInt(int position) {
        final PositionType positionType;

        switch (position) {
            case 1:
                positionType = POSITION_1;
                break;
            case 2:
                positionType = POSITION_2;
                break;
            case 3:
                positionType = POSITION_3;
                break;
            case 4:
                positionType = POSITION_4;
                break;
            case 5:
                positionType = POSITION_5;
                break;
            case 6:
                positionType = POSITION_6;
                break;
            default:
                positionType = BENCH;
                break;
        }

        return positionType;
    }

    public static List<PositionType> listPositions(GameType gameType) {
        List<PositionType> positions = new ArrayList<>();

        switch (gameType) {
            case BEACH:
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
                break;
            case INDOOR:
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
                positions.add(PositionType.POSITION_3);
                positions.add(PositionType.POSITION_4);
                positions.add(PositionType.POSITION_5);
                positions.add(PositionType.POSITION_6);
                break;
            case INDOOR_4X4:
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
                positions.add(PositionType.POSITION_3);
                positions.add(PositionType.POSITION_4);
                break;
            case SNOW:
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
                positions.add(PositionType.POSITION_3);
                break;
            default:
                break;
        }

        return positions;
    }
}
