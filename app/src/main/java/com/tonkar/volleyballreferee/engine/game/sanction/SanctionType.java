package com.tonkar.volleyballreferee.engine.game.sanction;

public enum SanctionType {
    YELLOW,
    RED,
    RED_EXPULSION,
    RED_DISQUALIFICATION,
    DELAY_WARNING,
    DELAY_PENALTY;

    public static String toLetter(SanctionType sanctionType) {
        return switch (sanctionType) {
            case DELAY_WARNING -> "DW";
            case DELAY_PENALTY -> "DP";
            case RED -> "R";
            case RED_EXPULSION -> "RE";
            case RED_DISQUALIFICATION -> "RD";
            default -> "Y";
        };
    }

    public static SanctionType fromLetter(String letter) {
        return switch (letter) {
            case "DW" -> DELAY_WARNING;
            case "DP" -> DELAY_PENALTY;
            case "R" -> RED;
            case "RE" -> RED_EXPULSION;
            case "RD" -> RED_DISQUALIFICATION;
            default -> YELLOW;
        };
    }

    public int seriousness() {
        return switch (this) {
            case DELAY_WARNING, DELAY_PENALTY -> -1;
            case RED -> 2;
            case RED_EXPULSION -> 3;
            case RED_DISQUALIFICATION -> 4;
            default -> 1;
        };
    }

    public boolean isDelaySanctionType() {
        return DELAY_WARNING.equals(this) || DELAY_PENALTY.equals(this);
    }

    public boolean isMisconductSanctionType() {
        return !isDelaySanctionType();
    }

    public boolean isMisconductRedCard() {
        return RED.equals(this);
    }

    public boolean isMisconductExpulsionCard() {
        return RED_EXPULSION.equals(this);
    }

    public boolean isMisconductDisqualificationCard() {
        return RED_DISQUALIFICATION.equals(this);
    }
}
