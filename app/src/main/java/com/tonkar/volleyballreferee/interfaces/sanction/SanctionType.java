package com.tonkar.volleyballreferee.interfaces.sanction;

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
}
