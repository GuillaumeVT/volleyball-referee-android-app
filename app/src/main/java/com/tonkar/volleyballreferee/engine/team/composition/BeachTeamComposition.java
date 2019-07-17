package com.tonkar.volleyballreferee.engine.team.composition;

import com.tonkar.volleyballreferee.engine.team.definition.BeachTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.BeachPlayer;
import com.tonkar.volleyballreferee.engine.team.player.Player;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

public class BeachTeamComposition extends TeamComposition {

    public BeachTeamComposition(final TeamDefinition teamDefinition) {
        super(teamDefinition);

        substitutePlayer(1, PositionType.POSITION_1);
        substitutePlayer(2, PositionType.POSITION_2);
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
    public boolean substitutePlayer(final int number, final PositionType positionType) {
        boolean result = false;

        if (PositionType.POSITION_1.equals(positionType) || PositionType.POSITION_2.equals(positionType) || PositionType.BENCH.equals(positionType)) {
            result = super.substitutePlayer(number, positionType);
        }

        return result;
    }

    @Override
    protected void onSubstitution(int oldNumber, int newNumber, PositionType positionType, int homeTeamPoints, int guestTeamPoints) {}

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof BeachTeamComposition) {
            BeachTeamComposition other = (BeachTeamComposition) obj;
            result = super.equals(other)
                    && (this.getPlayerAtPosition(PositionType.POSITION_1) == (other.getPlayerAtPosition(PositionType.POSITION_1)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_2) == (other.getPlayerAtPosition(PositionType.POSITION_2)));
        }

        return result;
    }

}
