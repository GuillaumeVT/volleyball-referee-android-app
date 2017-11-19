package com.tonkar.volleyballreferee.business.history;

public class RecordedTeam {

    private final String mName;
    private final int    mColor;

    public RecordedTeam(String name, int color) {
        mName = name;
        mColor = color;
    }

    String getName() {
        return mName;
    }

    int getColor() {
        return mColor;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedTeam) {
            RecordedTeam other = (RecordedTeam) obj;
            result = (this.getColor() == other.getColor())
                    && this.getName().equals(other.getName());
        }

        return result;
    }
}
