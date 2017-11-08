package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.Team;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.business.team.BeachTeam;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class BeachGame extends Game {

    BeachGame(final Rules rules) {
        super(GameType.BEACH, rules);
    }

    @Override
    protected Team createTeam(TeamType teamType) {
        return new BeachTeam(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetComplete()) {
            // In beach volley, the teams change sides every 7 points, or every 5 points during the tie break
            int period = isTieBreakSet() ? 5 : 7;
            int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

            if (getRules().isChangeSidesEvery7Points() && combinedScores > 0 && (combinedScores % period) == 0) {
                swapTeams(ActionOriginType.APPLICATION);
            }

            // In beach volley, there is one technical timeout when the combined score is 21 but not during tie break
            if (getRules().areTechnicalTimeoutsEnabled() && !isTieBreakSet() && combinedScores == 21) {
                notifyTechnicalTimeoutReached();
            }
        }
    }

    @Override
    public void removePoint(final TeamType teamType) {
        super.removePoint(teamType);

        // In beach volley, the teams change sides every 7 points, or every 5 points during the tie break
        int period = isTieBreakSet() ? 5 : 7;
        int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

        if (getRules().isChangeSidesEvery7Points() && combinedScores > 0 && (combinedScores % period) == (period - 1)) {
            swapTeams(ActionOriginType.APPLICATION);
        }
    }

    @Override
    public void substitutePlayer(TeamType teamType, int number, PositionType positionType) {
        super.substitutePlayer(teamType, number, positionType);

        int otherNumber = (number == 1) ? 2 : 1;
        PositionType otherPositionType = (PositionType.POSITION_1.equals(positionType) ? PositionType.POSITION_2 : PositionType.POSITION_1);
        super.substitutePlayer(teamType, otherNumber, otherPositionType);
    }

}
