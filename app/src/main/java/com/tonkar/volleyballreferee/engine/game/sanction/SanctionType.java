package com.tonkar.volleyballreferee.engine.game.sanction;

public enum SanctionType {
    YELLOW, RED, RED_EXPULSION, RED_DISQUALIFICATION, DELAY_WARNING, DELAY_PENALTY;

    public static String toLetter(SanctionType sanctionType) {
        String letter;

        switch (sanctionType) {
            case DELAY_WARNING:
                letter = "DW";
                break;
            case DELAY_PENALTY:
                letter = "DP";
                break;
            case RED:
                letter = "R";
                break;
            case RED_EXPULSION:
                letter = "RE";
                break;
            case RED_DISQUALIFICATION:
                letter = "RD";
                break;
            case YELLOW:
            default:
                letter = "Y";
                break;
        }

        return letter;
    }

    public static SanctionType fromLetter(String letter) {
        SanctionType sanctionType;

        switch (letter) {
            case "DW":
                sanctionType = DELAY_WARNING;
                break;
            case "DP":
                sanctionType = DELAY_PENALTY;
                break;
            case "R":
                sanctionType = RED;
                break;
            case "RE":
                sanctionType = RED_EXPULSION;
                break;
            case "RD":
                sanctionType = RED_DISQUALIFICATION;
                break;
            case "Y":
            default:
                sanctionType = YELLOW;
                break;
        }

        return sanctionType;
    }

    public int seriousness() {
        int seriousness;

        switch (this) {
            case DELAY_WARNING:
            case DELAY_PENALTY:
                seriousness = -1;
                break;
            case RED:
                seriousness = 2;
                break;
            case RED_EXPULSION:
                seriousness = 3;
                break;
            case RED_DISQUALIFICATION:
                seriousness = 4;
                break;
            case YELLOW:
            default:
                seriousness = 1;
                break;
        }

        return seriousness;
    }
}
