package com.tonkar.volleyballreferee.business.game;

import android.graphics.Color;

import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.team.BeachTeamDefinition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.BeachTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.business.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class BeachGame extends Game implements BeachTeamService {

    BeachGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        super(GameType.BEACH, id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    // For GSON Deserialization
    public BeachGame() {
        this("", "", "", 0L, 0L, new Rules());
    }

    @Override
    protected TeamDefinition createTeamDefinition(TeamType teamType) {
        return new BeachTeamDefinition(UUID.randomUUID().toString(), getCreatedBy(), teamType);
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
            int period = isTieBreakSet() ? getRules().getBeachCourtSwitchFreqTieBreak() : getRules().getBeachCourtSwitchFreq();
            int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

            if (getRules().isBeachCourtSwitches() && combinedScores > 0 && (combinedScores % period) == 0) {
                swapTeams(ActionOriginType.APPLICATION);
            }

            // In beach volley, there is one technical timeout when the combined score is 21 but not during tie break
            if (getRules().isTechnicalTimeouts() && !isTieBreakSet() && combinedScores == 21) {
                notifyTechnicalTimeoutReached();
            }
        }
    }

    @Override
    public void removeLastPoint() {
        super.removeLastPoint();

        // In beach volley, the teams change sides every 7 points, or every 5 points during the tie break
        int period = isTieBreakSet() ? getRules().getBeachCourtSwitchFreqTieBreak() : getRules().getBeachCourtSwitchFreq();
        int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

        if (getRules().isBeachCourtSwitches() && combinedScores > 0 && (combinedScores % period) == (period - 1)) {
            swapTeams(ActionOriginType.APPLICATION);
        }
    }

    @Override
    public void swapPlayers(TeamType teamType) {
        if (isFirstTimeServing(teamType)) {
            currentSet().getTeamComposition(teamType).rotateToNextPositions();
            notifyTeamRotated(teamType, true);
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

        if (ApiSanction.isPlayer(number) && SanctionType.RED_EXPULSION.equals(sanctionType)) {
            // The team is excluded for this set, the other team wins
            forceFinishSet(teamType.other());
        } else if (ApiSanction.isPlayer(number) && SanctionType.RED_DISQUALIFICATION.equals(sanctionType)) {
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
    public java.util.Set<ApiPlayer> getLiberos(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    public List<ApiSubstitution> getSubstitutions(TeamType teamType) {
        return new ArrayList<>();
    }

    @Override
    public List<ApiSubstitution> getSubstitutions(TeamType teamType, int setIndex) {
        return new ArrayList<>();
    }

    @Override
    public boolean isStartingLineupConfirmed() {
        return true;
    }

    @Override
    public boolean isStartingLineupConfirmed(int setIndex) {
        return true;
    }

    @Override
    public ApiCourt getStartingLineup(TeamType teamType, int setIndex) {
        return new ApiCourt();
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
    public void setCaptain(TeamType teamType, int number) {
        getTeamDefinition(teamType).setCaptain(number);
    }

    @Override
    public int getCaptain(TeamType teamType) {
        return getTeamDefinition(teamType).getCaptain();
    }

    @Override
    public java.util.Set<Integer> getPossibleCaptains(TeamType teamType) {
        return getTeamDefinition(teamType).getPossibleCaptains();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return getTeamDefinition(teamType).isCaptain(number);
    }

    @Override
    public void restoreGame(StoredGameService storedGameService) {
        if (GameStatus.LIVE.equals(storedGameService.getMatchStatus())) {
            startMatch();

            for (int setIndex = 0; setIndex < storedGameService.getNumberOfSets(); setIndex++) {
                List<TeamType> pointsLadder = storedGameService.getPointsLadder(setIndex);

                getSet(setIndex).setServingTeamAtStart(storedGameService.getFirstServingTeam(setIndex));

                for (TeamType scoringTeam : pointsLadder) {
                    int homePoints = getPoints(TeamType.HOME, setIndex);
                    int guestPoints = getPoints(TeamType.GUEST, setIndex);

                    List<ApiTimeout> homeTimeouts = storedGameService.getTimeoutsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (ApiTimeout timeout : homeTimeouts) {
                        callTimeout(TeamType.HOME);
                    }

                    List<ApiTimeout> guestTimeouts = storedGameService.getTimeoutsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (ApiTimeout timeout : guestTimeouts) {
                        callTimeout(TeamType.GUEST);
                    }

                    List<ApiSanction> homeSanctions = storedGameService.getSanctionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (ApiSanction sanction : homeSanctions) {
                        giveSanction(TeamType.HOME, sanction.getCard(), sanction.getNum());
                    }

                    List<ApiSanction> guestSanctions = storedGameService.getSanctionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (ApiSanction sanction : guestSanctions) {
                        giveSanction(TeamType.GUEST, sanction.getCard(), sanction.getNum());
                    }

                    addPoint(scoringTeam);
                }
            }
        }
    }
}
