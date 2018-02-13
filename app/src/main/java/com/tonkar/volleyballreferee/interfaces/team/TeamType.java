package com.tonkar.volleyballreferee.interfaces.team;

public enum TeamType {
    HOME, GUEST;

    public TeamType other() {
        if (HOME.equals(this)) {
            return GUEST;
        } else {
            return HOME;
        }
    }


    public static String toLetter(TeamType teamType) {
        String letter;

        switch (teamType) {
            case HOME:
                letter = "H";
                break;
            case GUEST:
            default:
                letter = "G";
                break;
        }

        return letter;
    }

    public static TeamType fromLetter(String letter) {
        TeamType teamType;

        switch (letter) {
            case "H":
                teamType = HOME;
                break;
            case "G":
            default:
                teamType = GUEST;
                break;
        }

        return teamType;
    }
}
