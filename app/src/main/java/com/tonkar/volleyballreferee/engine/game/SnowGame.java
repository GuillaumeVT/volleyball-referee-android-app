package com.tonkar.volleyballreferee.engine.game;

import android.graphics.Color;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.set.SnowSet;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.composition.SnowTeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.*;

public class SnowGame extends Game implements IClassicTeam {

    SnowGame(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        super(GameType.SNOW, id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    // For GSON Deserialization
    public SnowGame() {
        this("", "", "", 0L, 0L, new Rules());
    }

    @Override
    protected TeamDefinition createTeamDefinition(TeamType teamType) {
        return new SnowTeamDefinition(UUID.randomUUID().toString(), getCreatedBy(), teamType);
    }

    @Override
    protected com.tonkar.volleyballreferee.engine.game.set.Set createSet(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        return new SnowSet(getRules(), pointsToWinSet, servingTeamAtStart, getTeamDefinition(TeamType.HOME),
                           getTeamDefinition(TeamType.GUEST));
    }

    private SnowTeamComposition getSnowTeamComposition(TeamType teamType) {
        return (SnowTeamComposition) currentSet().getTeamComposition(teamType);
    }

    private SnowTeamComposition getSnowTeamComposition(TeamType teamType, int setIndex) {
        return (SnowTeamComposition) getSet(setIndex).getTeamComposition(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
            // In snow volley, the teams change sides every 5 points
            int period = getRules().getBeachCourtSwitchFreq();
            int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

            if (getRules().isBeachCourtSwitches() && combinedScores > 0 && (combinedScores % period) == 0) {
                swapTeams(ActionOriginType.APPLICATION);
            }
        }
    }

    @Override
    public void removeLastPoint() {
        super.removeLastPoint();

        // In snow volley, the teams change sides every 5 points
        int period = getRules().getBeachCourtSwitchFreq();
        int combinedScores = currentSet().getPoints(TeamType.HOME) + currentSet().getPoints(TeamType.GUEST);

        if (getRules().isBeachCourtSwitches() && combinedScores > 0 && (combinedScores % period) == (period - 1)) {
            swapTeams(ActionOriginType.APPLICATION);
        }
    }

    @Override
    void rotateToNextPositions(TeamType teamType) {
        if (canRotateForward(teamType)) {
            super.rotateToNextPositions(teamType);
        }
    }

    @Override
    void rotateToPreviousPositions(TeamType teamType) {
        if (canRotateBackward(teamType)) {
            super.rotateToPreviousPositions(teamType);
        }
    }

    private boolean canRotateForward(TeamType teamType) {
        // Must either be the first serving team, or the second serving team already served (= has more than 1 point)
        return currentSet().getServingTeamAtStart().equals(teamType) || getPoints(teamType) > 1;
    }

    private boolean canRotateBackward(TeamType teamType) {
        // Must either be the first serving team, or the second serving team already served (= has more than 0 point)
        return currentSet().getServingTeamAtStart().equals(teamType) || getPoints(teamType) > 0;
    }

    @Override
    public void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (getSnowTeamComposition(teamType).substitutePlayer(number, positionType, getPoints(TeamType.HOME), getPoints(TeamType.GUEST),
                                                              actionOriginType)) {
            notifyPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    @Override
    protected void undoSubstitution(TeamType teamType, SubstitutionDto substitution) {
        java.util.Set<Integer> evictedPlayers = getEvictedPlayersForCurrentSet(teamType, true, true);
        if (!evictedPlayers.contains(substitution.getPlayerIn()) && !evictedPlayers.contains(
                substitution.getPlayerOut()) && getSnowTeamComposition(teamType).undoSubstitution(substitution)) {
            notifyPlayerChanged(teamType, substitution.getPlayerOut(),
                                getSnowTeamComposition(teamType).getPlayerPosition(substitution.getPlayerOut()),
                                ActionOriginType.APPLICATION);
        }
    }

    @Override
    public java.util.Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType) {
        return getSnowTeamComposition(teamType).getPossibleSubstitutions(positionType);
    }

    @Override
    public void confirmStartingLineup(TeamType teamType) {
        getSnowTeamComposition(teamType).confirmStartingLineup();
        notifyStartingLineupSubmitted(teamType);
    }

    @Override
    public boolean hasGameCaptainOnCourt(TeamType teamType) {
        return getSnowTeamComposition(teamType).hasGameCaptainOnCourt();
    }

    @Override
    public int getGameCaptain(TeamType teamType, int setIndex) {
        int number = -1;

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            SnowTeamComposition snowTeamComposition = (SnowTeamComposition) set.getTeamComposition(teamType);
            number = snowTeamComposition.getGameCaptain();
        }

        return number;
    }

    @Override
    public boolean isGameCaptain(TeamType teamType, int number) {
        return getSnowTeamComposition(teamType).isGameCaptain(number);
    }

    @Override
    public void setGameCaptain(TeamType teamType, int number) {
        getSnowTeamComposition(teamType).setGameCaptain(number);
    }

    @Override
    public Set<Integer> getPossibleSecondaryCaptains(TeamType teamType) {
        return getSnowTeamComposition(teamType).getPossibleSecondaryCaptains();
    }

    @Override
    public boolean hasRemainingSubstitutions(TeamType teamType) {
        return getSnowTeamComposition(teamType).canSubstitute();
    }

    @Override
    public int countRemainingSubstitutions(TeamType teamType) {
        return getSnowTeamComposition(teamType).countRemainingSubstitutions();
    }

    @Override
    public java.util.Set<Integer> filterSubstitutionsWithEvictedPlayersForCurrentSet(TeamType teamType,
                                                                                     int evictedNumber,
                                                                                     java.util.Set<Integer> possibleSubstitutions) {
        final java.util.Set<Integer> filteredSubstitutions = new HashSet<>(possibleSubstitutions);
        final java.util.Set<Integer> evictedNumbers = getEvictedPlayersForCurrentSet(teamType, true, true);

        filteredSubstitutions.removeIf(evictedNumbers::contains);

        return filteredSubstitutions;
    }

    @Override
    public int getWaitingMiddleBlocker(TeamType teamType) {
        return -1;
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType) {
        return getSnowTeamComposition(teamType).isStartingLineupConfirmed();
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType, int setIndex) {
        return getSnowTeamComposition(teamType, setIndex).isStartingLineupConfirmed();
    }

    @Override
    public CourtDto getStartingLineup(TeamType teamType, int setIndex) {
        CourtDto startingLineup = new CourtDto();

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            SnowTeamComposition snowTeamComposition = (SnowTeamComposition) set.getTeamComposition(teamType);
            startingLineup = snowTeamComposition.getStartingLineup();
        }

        return startingLineup;
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        PositionType positionType = null;

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            SnowTeamComposition snowTeamComposition = (SnowTeamComposition) set.getTeamComposition(teamType);
            positionType = snowTeamComposition.getPlayerPositionInStartingLineup(number);
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        int number = -1;

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            SnowTeamComposition snowTeamComposition = (SnowTeamComposition) set.getTeamComposition(teamType);
            number = snowTeamComposition.getPlayerAtPositionInStartingLineup(positionType);
        }

        return number;
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
    public List<SubstitutionDto> getSubstitutions(TeamType teamType) {
        return getSnowTeamComposition(teamType).getSubstitutionsCopy();
    }

    @Override
    public List<SubstitutionDto> getSubstitutions(TeamType teamType, int setIndex) {
        List<SubstitutionDto> substitutions = new ArrayList<>();

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            SnowTeamComposition snowTeamComposition = (SnowTeamComposition) set.getTeamComposition(teamType);
            substitutions = snowTeamComposition.getSubstitutionsCopy();
        }

        return substitutions;
    }

    @Override
    public void giveSanction(TeamType teamType, SanctionType sanctionType, int number) {
        super.giveSanction(teamType, sanctionType, number);

        if (SanctionDto.isPlayer(number) && (sanctionType.isMisconductExpulsionCard() || sanctionType.isMisconductDisqualificationCard())) {
            // The player excluded for the set/match has to be legally replaced
            PositionType positionType = getPlayerPosition(teamType, number);

            if (!PositionType.BENCH.equals(positionType)) {
                final java.util.Set<Integer> possibleSubstitutions = getPossibleSubstitutions(teamType, positionType);
                final java.util.Set<Integer> filteredSubstitutions = filterSubstitutionsWithEvictedPlayersForCurrentSet(teamType, number,
                                                                                                                        possibleSubstitutions);

                // If there is no possible legal substitution, the set is lost
                if (filteredSubstitutions.size() == 0) {
                    forceFinishSet(teamType.other());
                }
            }
        }

        if (SanctionDto.isPlayer(number) && sanctionType.isMisconductDisqualificationCard() && !isMatchCompleted()) {
            // check that the team has enough players to continue the match
            // copy the list of players
            List<PlayerDto> players = new ArrayList<>(getTeamDefinition(teamType).getPlayers());

            // Remove the disqualified players

            for (SanctionDto sanction : getAllSanctions(teamType)) {
                if (sanction.getCard().isMisconductDisqualificationCard()) {
                    players.remove(new PlayerDto(sanction.getNum()));
                }
            }

            if (players.size() < getExpectedNumberOfPlayersOnCourt()) {
                // not enough players: finish the match
                forceFinishMatch(teamType.other());
            }
        }
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return getTeamDefinition(TeamType.HOME).getExpectedNumberOfPlayersOnCourt();
    }

    @Override
    public void restoreTeams(IStoredGame storedGame) {
        super.restoreTeams(storedGame);
    }

    @Override
    void restoreTeam(IStoredGame storedGame, TeamType teamType) {
        super.restoreTeam(storedGame, teamType);
        setLiberoColor(teamType, Color.parseColor(TeamDefinition.DEFAULT_COLOR));
        setCaptain(teamType, storedGame.getCaptain(teamType));
    }

    @Override
    public void restoreGame(IStoredGame storedGame) {
        if (GameStatus.LIVE.equals(storedGame.getMatchStatus())) {
            startMatch();

            for (int setIndex = 0; setIndex < storedGame.getNumberOfSets(); setIndex++) {
                List<TeamType> pointsLadder = storedGame.getPointsLadder(setIndex);

                if (storedGame.isStartingLineupConfirmed(TeamType.HOME)) {
                    CourtDto homeStartingLineup = storedGame.getStartingLineup(TeamType.HOME, setIndex);

                    for (PositionType position : PositionType.listPositions(getKind())) {
                        substitutePlayer(TeamType.HOME, homeStartingLineup.getPlayerAt(position), position, ActionOriginType.USER);
                    }

                    confirmStartingLineup(TeamType.HOME);
                }

                if (storedGame.isStartingLineupConfirmed(TeamType.GUEST)) {
                    CourtDto guestStartingLineup = storedGame.getStartingLineup(TeamType.GUEST, setIndex);

                    for (PositionType position : PositionType.listPositions(getKind())) {
                        substitutePlayer(TeamType.GUEST, guestStartingLineup.getPlayerAt(position), position, ActionOriginType.USER);
                    }

                    confirmStartingLineup(TeamType.GUEST);
                }

                getSet(setIndex).setServingTeamAtStart(storedGame.getFirstServingTeam(setIndex));

                for (int pointsIndex = 0; pointsIndex < pointsLadder.size(); pointsIndex++) {
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

                    List<SubstitutionDto> homeSubstitutions = storedGame.getSubstitutionsIfExist(TeamType.HOME, setIndex, homePoints,
                                                                                                 guestPoints);
                    for (SubstitutionDto substitution : homeSubstitutions) {
                        PositionType positionType = getPlayerPosition(TeamType.HOME, substitution.getPlayerOut(), setIndex);
                        substitutePlayer(TeamType.HOME, substitution.getPlayerIn(), positionType, ActionOriginType.USER);
                    }

                    List<SubstitutionDto> guestSubstitutions = storedGame.getSubstitutionsIfExist(TeamType.GUEST, setIndex, homePoints,
                                                                                                  guestPoints);
                    for (SubstitutionDto substitution : guestSubstitutions) {
                        PositionType positionType = getPlayerPosition(TeamType.GUEST, substitution.getPlayerOut(), setIndex);
                        substitutePlayer(TeamType.GUEST, substitution.getPlayerIn(), positionType, ActionOriginType.USER);
                    }

                    List<SanctionDto> homeSanctions = storedGame.getSanctionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (SanctionDto sanction : homeSanctions) {
                        giveSanction(TeamType.HOME, sanction.getCard(), sanction.getNum());
                    }

                    List<SanctionDto> guestSanctions = storedGame.getSanctionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (SanctionDto sanction : guestSanctions) {
                        giveSanction(TeamType.GUEST, sanction.getCard(), sanction.getNum());
                    }

                    if (pointsIndex == pointsLadder.size() - 1) {
                        setGameCaptain(TeamType.HOME, storedGame.getGameCaptain(TeamType.HOME, setIndex));
                        setGameCaptain(TeamType.GUEST, storedGame.getGameCaptain(TeamType.GUEST, setIndex));
                    }

                    addPoint(pointsLadder.get(pointsIndex));
                }
            }
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

}
