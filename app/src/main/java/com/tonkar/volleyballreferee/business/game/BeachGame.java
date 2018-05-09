package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.BeachTeamDefinition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.BeachTeamService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public class BeachGame extends Game implements BeachTeamService {

    BeachGame(final String refereeName, final UserId userId) {
        super(GameType.BEACH, refereeName, userId);
    }

    // For GSON Deserialization
    public BeachGame() {
        this("", UserId.VBR_USER_ID);
    }

    @Override
    protected TeamDefinition createTeamDefinition(TeamType teamType) {
        return new BeachTeamDefinition(teamType);
    }

    @Override
    protected Set createSet(Rules rules, boolean isTieBreakSet, TeamType servingTeamAtStart, TeamDefinition homeTeamDefinition, TeamDefinition guestTeamDefinition) {
        return new BeachSet(rules, isTieBreakSet ? rules.getPointsInTieBreak() : rules.getPointsPerSet(), servingTeamAtStart, homeTeamDefinition, guestTeamDefinition);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
            // In beach volley, the teams change sides every 7 points, or every 5 points during the tie break
            int period = isTieBreakSet() ? getRules().getChangeSidesPeriodTieBreak() : getRules().getChangeSidesPeriod();
            int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

            if (getRules().isChangeSidesBeach() && combinedScores > 0 && (combinedScores % period) == 0) {
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
        int period = isTieBreakSet() ? getRules().getChangeSidesPeriodTieBreak() : getRules().getChangeSidesPeriod();
        int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

        if (getRules().isChangeSidesBeach() && combinedScores > 0 && (combinedScores % period) == (period - 1)) {
            swapTeams(ActionOriginType.APPLICATION);
        }
    }

    @Override
    public void swapPlayers(TeamType teamType) {
        if (isFirstTimeServing(teamType)) {
            currentSet().getTeamComposition(teamType).rotateToNextPositions();
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

    @Override
    public void giveSanction(TeamType teamType, SanctionType sanctionType, int number) {
        super.giveSanction(teamType, sanctionType, number);

        if (SanctionType.RED_EXPULSION.equals(sanctionType)) {
            // The team is excluded for this set, the other team wins
            forceFinishSet(teamType.other());
        } else if (SanctionType.RED_DISQUALIFICATION.equals(sanctionType)) {
            // The team is excluded for this match, the other team wins
            forceFinishMatch(teamType.other());
        }
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return 2;
    }
}
