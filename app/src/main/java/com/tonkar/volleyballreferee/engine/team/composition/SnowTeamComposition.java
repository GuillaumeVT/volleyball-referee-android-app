package com.tonkar.volleyballreferee.engine.team.composition;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.definition.*;
import com.tonkar.volleyballreferee.engine.team.player.*;

public class SnowTeamComposition extends ClassicTeamComposition {

    public SnowTeamComposition(final TeamDefinition teamDefinition, int substitutionType, int maxSubstitutionsPerSet) {
        super(teamDefinition, substitutionType, maxSubstitutionsPerSet);
    }

    // For GSON Deserialization
    public SnowTeamComposition() {
        this(new SnowTeamDefinition(), Rules.NO_LIMITATION, 0);
    }

    @Override
    protected Player createPlayer(int number) {
        return new SnowPlayer(number);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof SnowTeamComposition other) {
            result = super.equals(other) && (this.getPlayerAtPosition(PositionType.POSITION_1) == (other.getPlayerAtPosition(
                    PositionType.POSITION_1))) && (this.getPlayerAtPosition(PositionType.POSITION_2) == (other.getPlayerAtPosition(
                    PositionType.POSITION_2))) && (this.getPlayerAtPosition(PositionType.POSITION_3) == (other.getPlayerAtPosition(
                    PositionType.POSITION_3))) && (this.isStartingLineupConfirmed() == other.isStartingLineupConfirmed()) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_1) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_1))) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_2) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_2))) && (this.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_3) == (other.getPlayerAtPositionInStartingLineup(
                    PositionType.POSITION_3))) && (this.canSubstitute() == other.canSubstitute()) && (this
                    .getSubstitutionsCopy()
                    .equals(other.getSubstitutionsCopy())) && (this.getSecondaryCaptain() == other.getSecondaryCaptain());
        }

        return result;
    }

}
