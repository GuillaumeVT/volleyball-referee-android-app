package com.tonkar.volleyballreferee.engine.team.player;

import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.*;

public enum PositionType {

    POSITION_1,
    POSITION_2,
    POSITION_3,
    POSITION_4,
    POSITION_5,
    POSITION_6,
    BENCH;

    public PositionType previousPosition(GameType gameType) {
        PositionType previous = BENCH;

        if (GameType.INDOOR.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> previous = POSITION_2;
                case POSITION_2 -> previous = POSITION_3;
                case POSITION_3 -> previous = POSITION_4;
                case POSITION_4 -> previous = POSITION_5;
                case POSITION_5 -> previous = POSITION_6;
                case POSITION_6 -> previous = POSITION_1;
                default -> {
                }
            }
        } else if (GameType.BEACH.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> previous = POSITION_2;
                case POSITION_2 -> previous = POSITION_1;
                default -> {
                }
            }
        } else if (GameType.INDOOR_4X4.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> previous = POSITION_2;
                case POSITION_2 -> previous = POSITION_3;
                case POSITION_3 -> previous = POSITION_4;
                case POSITION_4 -> previous = POSITION_1;
                default -> {
                }
            }
        } else if (GameType.SNOW.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> previous = POSITION_2;
                case POSITION_2 -> previous = POSITION_3;
                case POSITION_3 -> previous = POSITION_1;
                default -> {
                }
            }
        }

        return previous;
    }

    public PositionType nextPosition(GameType gameType) {
        PositionType next = BENCH;

        if (GameType.INDOOR.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> next = POSITION_6;
                case POSITION_2 -> next = POSITION_1;
                case POSITION_3 -> next = POSITION_2;
                case POSITION_4 -> next = POSITION_3;
                case POSITION_5 -> next = POSITION_4;
                case POSITION_6 -> next = POSITION_5;
                default -> {
                }
            }
        } else if (GameType.BEACH.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> next = POSITION_2;
                case POSITION_2 -> next = POSITION_1;
                default -> {
                }
            }
        } else if (GameType.INDOOR_4X4.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> next = POSITION_4;
                case POSITION_2 -> next = POSITION_1;
                case POSITION_3 -> next = POSITION_2;
                case POSITION_4 -> next = POSITION_3;
                default -> {
                }
            }
        } else if (GameType.SNOW.equals(gameType)) {
            switch (this) {
                case POSITION_1 -> next = POSITION_3;
                case POSITION_2 -> next = POSITION_1;
                case POSITION_3 -> next = POSITION_2;
                default -> {
                }
            }
        }

        return next;
    }

    public boolean isAtTheBack() {
        return switch (this) {
            case POSITION_1, POSITION_5, POSITION_6 -> true;
            default -> false;
        };
    }

    public PositionType oppositePosition() {
        return switch (this) {
            case POSITION_1 -> POSITION_4;
            case POSITION_2 -> POSITION_5;
            case POSITION_3 -> POSITION_6;
            case POSITION_4 -> POSITION_1;
            case POSITION_5 -> POSITION_2;
            case POSITION_6 -> POSITION_3;
            default -> BENCH;
        };
    }

    public static int toInt(PositionType positionType) {
        return switch (positionType) {
            case POSITION_1 -> 1;
            case POSITION_2 -> 2;
            case POSITION_3 -> 3;
            case POSITION_4 -> 4;
            case POSITION_5 -> 5;
            case POSITION_6 -> 6;
            default -> 0;
        };
    }

    public static PositionType fromInt(int position) {
        return switch (position) {
            case 1 -> POSITION_1;
            case 2 -> POSITION_2;
            case 3 -> POSITION_3;
            case 4 -> POSITION_4;
            case 5 -> POSITION_5;
            case 6 -> POSITION_6;
            default -> BENCH;
        };
    }

    public static List<PositionType> listPositions(GameType gameType) {
        List<PositionType> positions = new ArrayList<>();

        switch (gameType) {
            case BEACH -> {
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
            }
            case INDOOR -> {
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
                positions.add(PositionType.POSITION_3);
                positions.add(PositionType.POSITION_4);
                positions.add(PositionType.POSITION_5);
                positions.add(PositionType.POSITION_6);
            }
            case INDOOR_4X4 -> {
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
                positions.add(PositionType.POSITION_3);
                positions.add(PositionType.POSITION_4);
            }
            case SNOW -> {
                positions.add(PositionType.POSITION_1);
                positions.add(PositionType.POSITION_2);
                positions.add(PositionType.POSITION_3);
            }
            default -> {
            }
        }

        return positions;
    }
}
