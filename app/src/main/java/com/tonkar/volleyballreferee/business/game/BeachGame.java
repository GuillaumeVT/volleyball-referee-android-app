package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.Team;
import com.tonkar.volleyballreferee.interfaces.BeachTeamService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.business.team.BeachTeam;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class BeachGame extends Game implements BeachTeamService {

    BeachGame(final Rules rules) {
        super(GameType.BEACH, rules);
    }

    @Override
    protected Team createTeam(TeamType teamType) {
        return new BeachTeam(teamType);
    }

    private BeachTeam getBeachTeam(TeamType teamType) {
        return (BeachTeam) getTeam(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
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
    public void removeLastPoint() {
        super.removeLastPoint();

        // In beach volley, the teams change sides every 7 points, or every 5 points during the tie break
        int period = isTieBreakSet() ? 5 : 7;
        int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

        if (getRules().isChangeSidesEvery7Points() && combinedScores > 0 && (combinedScores % period) == (period - 1)) {
            swapTeams(ActionOriginType.APPLICATION);
        }
    }

    @Override
    protected void onNewSet() {}

    @Override
    public void swapPlayers(TeamType teamType) {
        if (isFirstTimeServing(teamType)) {
            getBeachTeam(teamType).rotateToNextPositions();
            notifyTeamRotated(teamType);
        }
    }

    private boolean isFirstTimeServing(TeamType teamType) {
        boolean first = false;

        TeamType servingTeamAtStart = currentSet().getServingTeamAtStart();
        int servingTeamPoints = getPoints(teamType);

        if (getServingTeam().equals(teamType) && servingTeamAtStart.equals(teamType) && servingTeamPoints == 0) {
            first = true;
        } else if (getServingTeam().equals(teamType) && !servingTeamAtStart.equals(teamType) && servingTeamPoints == 1) {
            first = true;
        }

        return first;
    }
}
