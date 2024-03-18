package com.tonkar.volleyballreferee.engine.team.composition;

import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.team.definition.*;
import com.tonkar.volleyballreferee.engine.team.player.*;

public class BeachTeamComposition extends TeamComposition {

    public BeachTeamComposition(final TeamDefinition teamDefinition) {
        super(teamDefinition);

        substitutePlayer(1, PositionType.POSITION_1, ActionOriginType.APPLICATION);
        substitutePlayer(2, PositionType.POSITION_2, ActionOriginType.APPLICATION);
    }

    // For GSON Deserialization
    public BeachTeamComposition() {
        this(new BeachTeamDefinition());
    }

    @Override
    protected Player createPlayer(int number) {
        return new BeachPlayer(number);
    }

    @Override
    public boolean substitutePlayer(final int number, final PositionType positionType, ActionOriginType actionOriginType) {
        boolean result = false;

        if (PositionType.POSITION_1.equals(positionType) || PositionType.POSITION_2.equals(positionType) || PositionType.BENCH.equals(
                positionType)) {
            result = super.substitutePlayer(number, positionType, actionOriginType);
        }

        return result;
    }

    @Override
    protected void onSubstitution(int oldNumber,
                                  int newNumber,
                                  PositionType positionType,
                                  int homeTeamPoints,
                                  int guestTeamPoints,
                                  ActionOriginType actionOriginType) {}

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof BeachTeamComposition other) {
            result = super.equals(other) && (this.getPlayerAtPosition(PositionType.POSITION_1) == (other.getPlayerAtPosition(
                    PositionType.POSITION_1))) && (this.getPlayerAtPosition(PositionType.POSITION_2) == (other.getPlayerAtPosition(
                    PositionType.POSITION_2)));
        }

        return result;
    }

}
