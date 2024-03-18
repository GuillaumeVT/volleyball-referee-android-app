package com.tonkar.volleyballreferee.engine.team;

public enum TeamType {
    HOME,
    GUEST;

    public TeamType other() {
        if (HOME.equals(this)) {
            return GUEST;
        } else {
            return HOME;
        }
    }

    public static String toLetter(TeamType teamType) {
        return switch (teamType) {
            case HOME -> "H";
            default -> "G";
        };
    }

    public static TeamType fromLetter(String letter) {
        return switch (letter) {
            case "H" -> HOME;
            default -> GUEST;
        };
    }
}
