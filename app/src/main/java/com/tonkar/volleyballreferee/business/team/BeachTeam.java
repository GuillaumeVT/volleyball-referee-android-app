package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class BeachTeam extends Team {

    public BeachTeam(final TeamType teamType) {
        super(teamType);

        addPlayer(1);
        addPlayer(2);
        substitutePlayer(1, PositionType.POSITION_1);
        substitutePlayer(2, PositionType.POSITION_2);
    }

    @Override
    protected Player createPlayer(int number) {
        return new BeachPlayer(number);
    }

    @Override
    public boolean substitutePlayer(final int number, final PositionType positionType) {
        boolean result = false;

        if (PositionType.POSITION_1.equals(positionType) || PositionType.POSITION_2.equals(positionType) || PositionType.BENCH.equals(positionType)) {
            result = super.substitutePlayer(number, positionType);
        }

        return result;
    }

    @Override
    protected void onSubstitution(int oldNumber, int newNumber, PositionType positionType) {}

}
