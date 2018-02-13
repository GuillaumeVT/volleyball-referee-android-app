package com.tonkar.volleyballreferee.interfaces.card;

public enum PenaltyCardType {
    YELLOW, RED, RED_EXPULSION, RED_DISQUALIFICATION;

    public static String toLetter(PenaltyCardType penaltyCardType) {
        String letter;

        switch (penaltyCardType) {
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

    public static PenaltyCardType fromLetter(String letter) {
        PenaltyCardType penaltyCardType;

        switch (letter) {
            case "R":
                penaltyCardType = RED;
                break;
            case "RE":
                penaltyCardType = RED_EXPULSION;
                break;
            case "RD":
                penaltyCardType = RED_DISQUALIFICATION;
                break;
            case "Y":
            default:
                penaltyCardType = YELLOW;
                break;
        }

        return penaltyCardType;
    }
}
