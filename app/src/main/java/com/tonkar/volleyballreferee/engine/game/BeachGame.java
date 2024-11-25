package com.tonkar.volleyballreferee.engine.game;

import android.graphics.Color;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.set.Set;
import com.tonkar.volleyballreferee.engine.game.set.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.definition.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.*;

public class BeachGame extends Game implements IBeachTeam {

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
        return new BeachSet(getRules(), pointsToWinSet, servingTeamAtStart, getTeamDefinition(TeamType.HOME),
                            getTeamDefinition(TeamType.GUEST));
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

        if (SanctionDto.isPlayer(number) && sanctionType.isMisconductExpulsionCard()) {
            // The team is excluded for this set, the other team wins
            forceFinishSet(teamType.other());
        } else if (SanctionDto.isPlayer(number) && sanctionType.isMisconductDisqualificationCard()) {
            // The team is excluded for this match, the other team wins
            forceFinishMatch(teamType.other());
        }
    }

    @Override
    public java.util.Set<SanctionType> getPossibleMisconductSanctions(TeamType teamType, int number) {
        boolean teamHasReachedPenalty = false;

        for (SanctionDto sanction : getAllSanctions(teamType)) {
            if (sanction.getCard().isMisconductSanctionType()) {
                teamHasReachedPenalty = true;
            }
        }

        java.util.Set<SanctionType> possibleMisconductSanctions = new HashSet<>();

        possibleMisconductSanctions.add(SanctionType.YELLOW);
        possibleMisconductSanctions.add(SanctionType.RED);
        possibleMisconductSanctions.add(SanctionType.RED_EXPULSION);
        possibleMisconductSanctions.add(SanctionType.RED_DISQUALIFICATION);

        if (teamHasReachedPenalty) {
            possibleMisconductSanctions.remove(SanctionType.YELLOW);
        }

        int currentSetIndex = currentSetIndex();
        int numberOfRedCards = 0;

        // A player can have 2 red cards in the same set. On the third rude conduct the player is at least expulsed

        for (SanctionDto sanction : getPlayerSanctions(teamType, number)) {
            if (sanction.getSet() == currentSetIndex && sanction.getCard().isMisconductRedCard()) {
                numberOfRedCards++;
            }
        }

        if (numberOfRedCards >= 2) {
            possibleMisconductSanctions.remove(SanctionType.RED);
        }

        return possibleMisconductSanctions;
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
    public java.util.Set<PlayerDto> getLiberos(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    protected void undoSubstitution(TeamType teamType, SubstitutionDto substitution) {}

    @Override
    public List<SubstitutionDto> getSubstitutions(TeamType teamType) {
        return new ArrayList<>();
    }

    @Override
    public List<SubstitutionDto> getSubstitutions(TeamType teamType, int setIndex) {
        return new ArrayList<>();
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType) {
        return true;
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType, int setIndex) {
        return true;
    }

    @Override
    public CourtDto getStartingLineup(TeamType teamType, int setIndex) {
        return new CourtDto();
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
    public void restoreGame(IStoredGame storedGame) {
        if (GameStatus.LIVE.equals(storedGame.getMatchStatus())) {
            startMatch();

            for (int setIndex = 0; setIndex < storedGame.getNumberOfSets(); setIndex++) {
                List<TeamType> pointsLadder = storedGame.getPointsLadder(setIndex);

                getSet(setIndex).setServingTeamAtStart(storedGame.getFirstServingTeam(setIndex));

                for (TeamType scoringTeam : pointsLadder) {
                    int homePoints = getPoints(TeamType.HOME, setIndex);
                    int guestPoints = getPoints(TeamType.GUEST, setIndex);

                    List<TimeoutDto> homeTimeouts = storedGame.getTimeoutsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (TimeoutDto timeout : homeTimeouts) {
                        callTimeout(TeamType.HOME);
                    }

                    List<TimeoutDto> guestTimeouts = storedGame.getTimeoutsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (TimeoutDto timeout : guestTimeouts) {
                        callTimeout(TeamType.GUEST);
                    }

                    List<SanctionDto> homeSanctions = storedGame.getSanctionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (SanctionDto sanction : homeSanctions) {
                        giveSanction(TeamType.HOME, sanction.getCard(), sanction.getNum());
                    }

                    List<SanctionDto> guestSanctions = storedGame.getSanctionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (SanctionDto sanction : guestSanctions) {
                        giveSanction(TeamType.GUEST, sanction.getCard(), sanction.getNum());
                    }

                    addPoint(scoringTeam);
                }
            }
        }
    }
}
