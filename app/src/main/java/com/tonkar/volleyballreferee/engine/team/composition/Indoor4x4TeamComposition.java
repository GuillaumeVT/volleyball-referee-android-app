package com.tonkar.volleyballreferee.engine.team.composition;

import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.definition.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.Indoor4x4Player;
import com.tonkar.volleyballreferee.engine.team.player.Player;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

public class Indoor4x4TeamComposition extends ClassicTeamComposition {

    public Indoor4x4TeamComposition(final TeamDefinition teamDefinition, int substitutionType, int maxSubstitutionsPerSet) {
        super(teamDefinition, substitutionType, maxSubstitutionsPerSet);
    }

    // For GSON Deserialization
    public Indoor4x4TeamComposition() {
        this(new IndoorTeamDefinition(), Rules.NO_LIMITATION, 0);
    }

    @Override
    protected Player createPlayer(int number) {
        return new Indoor4x4Player(number);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof Indoor4x4TeamComposition) {
            Indoor4x4TeamComposition other = (Indoor4x4TeamComposition) obj;
            result = super.equals(other)
                    && (this.getPlayerAtPosition(PositionType.POSITION_1) == (other.getPlayerAtPosition(PositionType.POSITION_1)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_2) == (other.getPlayerAtPosition(PositionType.POSITION_2)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_3) == (other.getPlayerAtPosition(PositionType.POSITION_3)))
                    && (this.getPlayerAtPosition(PositionType.POSITION_4) == (other.getPlayerAtPosition(PositionType.POSITION_4)))
                    && (this.isStartingLineupConfirmed() == other.isStartingLineupConfirmed())
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_1) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_1)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_2) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_2)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_3) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_3)))
                    && (this.getPlayerAtPositionInStartingLineup(PositionType.POSITION_4) == (other.getPlayerAtPositionInStartingLineup(PositionType.POSITION_4)))
                    && (this.canSubstitute() == other.canSubstitute())
                    && (this.getSubstitutionsCopy().equals(other.getSubstitutionsCopy()))
                    && (this.getSecondaryCaptain() == other.getSecondaryCaptain());
        }

        return result;
    }

}
