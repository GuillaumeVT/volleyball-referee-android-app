package com.tonkar.volleyballreferee.business.game;

import android.graphics.Color;

import com.tonkar.volleyballreferee.business.team.BeachTeamDefinition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.BeachTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BeachGame extends Game implements BeachTeamService {

    BeachGame(final long gameDate, final long gameSchedule, final Rules rules) {
        super(GameType.BEACH, gameDate, gameSchedule, rules);
    }

    // For GSON Deserialization
    public BeachGame() {
        this(0L, 0L, Rules.defaultUniversalRules());
    }

    @Override
    protected TeamDefinition createTeamDefinition(TeamType teamType) {
        return new BeachTeamDefinition(teamType);
    }

    @Override
    protected Set createSet(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        return new BeachSet(getRules(), pointsToWinSet, servingTeamAtStart, getTeamDefinition(TeamType.HOME), getTeamDefinition(TeamType.GUEST));
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
            // In beach volley, the teams change sides every 7 points, or every 5 points during the tie break
            int period = isTieBreakSet() ? getRules().getBeachCourtSwitchFrequencyTieBreak() : getRules().getBeachCourtSwitchFrequency();
            int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

            if (getRules().areBeachCourtSwitchesEnabled() && combinedScores > 0 && (combinedScores % period) == 0) {
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
        int period = isTieBreakSet() ? getRules().getBeachCourtSwitchFrequencyTieBreak() : getRules().getBeachCourtSwitchFrequency();
        int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

        if (getRules().areBeachCourtSwitchesEnabled() && combinedScores > 0 && (combinedScores % period) == (period - 1)) {
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

        if (number > 0 && SanctionType.RED_EXPULSION.equals(sanctionType)) {
            // The team is excluded for this set, the other team wins
            forceFinishSet(teamType.other());
        } else if (number > 0 && SanctionType.RED_DISQUALIFICATION.equals(sanctionType)) {
            // The team is excluded for this match, the other team wins
            forceFinishMatch(teamType.other());
        }
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return getTeamDefinition(TeamType.HOME).getExpectedNumberOfPlayersOnCourt();
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return Color.parseColor(TeamDefinition.DEFAULT_COLOR);
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {}

    @Override
    public void addLibero(TeamType teamType, int number) {}

    @Override
    public void removeLibero(TeamType teamType, int number) {}

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return false;
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return false;
    }

    @Override
    public java.util.Set<Integer> getLiberos(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType) {
        return new ArrayList<>();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        return new ArrayList<>();
    }

    @Override
    public boolean isStartingLineupConfirmed() {
        return true;
    }

    @Override
    public java.util.Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex) {
        return new HashSet<>();
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        return null;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        return 0;
    }

    @Override
    public void setCaptain(TeamType teamType, int number) {}

    @Override
    public int getCaptain(TeamType teamType) {
        return 1;
    }

    @Override
    public java.util.Set<Integer> getPossibleCaptains(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return false;
    }

    @Override
    public void restoreGame(RecordedGameService recordedGameService) {
        if (GameStatus.LIVE.equals(recordedGameService.getMatchStatus())) {
            startMatch();

            for (int setIndex = 0; setIndex < recordedGameService.getNumberOfSets(); setIndex++) {
                List<TeamType> pointsLadder = recordedGameService.getPointsLadder(setIndex);

                getSet(setIndex).setServingTeamAtStart(recordedGameService.getFirstServingTeam(setIndex));

                for (TeamType scoringTeam : pointsLadder) {
                    int homePoints = getPoints(TeamType.HOME, setIndex);
                    int guestPoints = getPoints(TeamType.GUEST, setIndex);

                    List<Timeout> homeTimeouts = recordedGameService.getTimeoutsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (Timeout timeout : homeTimeouts) {
                        callTimeout(TeamType.HOME);
                    }

                    List<Timeout> guestTimeouts = recordedGameService.getTimeoutsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (Timeout timeout : guestTimeouts) {
                        callTimeout(TeamType.GUEST);
                    }

                    List<Sanction> homeSanctions = recordedGameService.getSanctionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (Sanction sanction : homeSanctions) {
                        giveSanction(TeamType.HOME, sanction.getSanctionType(), sanction.getPlayer());
                    }

                    List<Sanction> guestSanctions = recordedGameService.getSanctionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (Sanction sanction : guestSanctions) {
                        giveSanction(TeamType.GUEST, sanction.getSanctionType(), sanction.getPlayer());
                    }

                    addPoint(scoringTeam);
                }
            }
        }
    }
}
