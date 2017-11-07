package com.tonkar.volleyballreferee.business.history;

public class RecordedTeam {

    private final String mName;
    private final int    mColorId;

    public RecordedTeam(String name, int colorId) {
        mName = name;
        mColorId = colorId;
    }

    String getName() {
        return mName;
    }

    int getColorId() {
        return mColorId;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof RecordedTeam) {
            RecordedTeam other = (RecordedTeam) obj;
            result = (this.getColorId() == other.getColorId())
                    && this.getName().equals(other.getName());
        }

        return result;
    }
}
