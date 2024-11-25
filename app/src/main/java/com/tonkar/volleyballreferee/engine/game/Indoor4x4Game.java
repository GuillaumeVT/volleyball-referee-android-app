package com.tonkar.volleyballreferee.engine.game;

import android.graphics.Color;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.set.Indoor4x4Set;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.composition.Indoor4x4TeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.*;

public class Indoor4x4Game extends Game implements IClassicTeam {

    Indoor4x4Game(String id, String createdBy, String refereeName, long createdAt, long scheduledAt, Rules rules) {
        super(GameType.INDOOR_4X4, id, createdBy, refereeName, createdAt, scheduledAt, rules);
    }

    // For GSON Deserialization
    public Indoor4x4Game() {
        this("", "", "", 0L, 0L, new Rules());
    }

    @Override
    protected TeamDefinition createTeamDefinition(TeamType teamType) {
        return new IndoorTeamDefinition(GameType.INDOOR_4X4, UUID.randomUUID().toString(), getCreatedBy(), teamType);
    }

    @Override
    protected com.tonkar.volleyballreferee.engine.game.set.Set createSet(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        return new Indoor4x4Set(getRules(), pointsToWinSet, servingTeamAtStart, getTeamDefinition(TeamType.HOME),
                                getTeamDefinition(TeamType.GUEST));
    }

    private Indoor4x4TeamComposition getIndoorTeamComposition(TeamType teamType) {
        return (Indoor4x4TeamComposition) currentSet().getTeamComposition(teamType);
    }

    private Indoor4x4TeamComposition getIndoorTeamComposition(TeamType teamType, int setIndex) {
        return (Indoor4x4TeamComposition) getSet(setIndex).getTeamComposition(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
            // Record the last server so we can prevent him from coming back on position 1 for serving
            final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

            // In indoor volley, the teams change sides after the 8th during the tie break
            if (isTieBreakSet() && leadingScore == 8 && currentSet().getPoints(TeamType.HOME) != currentSet().getPoints(
                    TeamType.GUEST) && teamType.equals(currentSet().getLeadingTeam())) {
                swapTeams(ActionOriginType.APPLICATION);
            }

            // In indoor volley, there are two technical timeouts at 8 and 16 but not during tie break
            if (getRules().isTechnicalTimeouts() && !isTieBreakSet() && currentSet()
                    .getLeadingTeam()
                    .equals(teamType) && (leadingScore == 8 || leadingScore == 16) && currentSet().getPoints(
                    TeamType.HOME) != currentSet().getPoints(TeamType.GUEST)) {
                notifyTechnicalTimeoutReached();
            }

            // Specific custom rule
            if (samePlayerServedNConsecutiveTimes(teamType, getPoints(teamType), getPointsLadder())) {
                rotateToNextPositions(teamType);
            }
        }
    }

    @Override
    public void removeLastPoint() {
        final TeamType oldServingTeam = getServingTeam();
        final int oldLeadingScore = currentSet().getPoints(currentSet().getLeadingTeam());
        super.removeLastPoint();

        final TeamType newServingTeam = getServingTeam();
        final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

        // In indoor volley, the teams change sides after the 8th during the tie break
        if (isTieBreakSet() && leadingScore == 7 && oldLeadingScore == 8) {
            swapTeams(ActionOriginType.APPLICATION);
        }

        // Specific custom rule
        if (oldServingTeam.equals(newServingTeam) && samePlayerHadServedNConsecutiveTimes(oldServingTeam, getPoints(oldServingTeam),
                                                                                          getPointsLadder())) {
            rotateToPreviousPositions(oldServingTeam);
        }
    }

    @Override
    public void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (getIndoorTeamComposition(teamType).substitutePlayer(number, positionType, getPoints(TeamType.HOME), getPoints(TeamType.GUEST),
                                                                actionOriginType)) {
            notifyPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    @Override
    protected void undoSubstitution(TeamType teamType, SubstitutionDto substitution) {
        Set<Integer> evictedPlayers = getEvictedPlayersForCurrentSet(teamType, true, true);
        if (!evictedPlayers.contains(substitution.getPlayerIn()) && !evictedPlayers.contains(
                substitution.getPlayerOut()) && getIndoorTeamComposition(teamType).undoSubstitution(substitution)) {
            notifyPlayerChanged(teamType, substitution.getPlayerOut(),
                                getIndoorTeamComposition(teamType).getPlayerPosition(substitution.getPlayerOut()),
                                ActionOriginType.APPLICATION);
        }
    }

    @Override
    public Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType) {
        return getIndoorTeamComposition(teamType).getPossibleSubstitutions(positionType);
    }

    @Override
    public void confirmStartingLineup(TeamType teamType) {
        getIndoorTeamComposition(teamType).confirmStartingLineup();
        notifyStartingLineupSubmitted(teamType);
    }

    @Override
    public boolean hasGameCaptainOnCourt(TeamType teamType) {
        return getIndoorTeamComposition(teamType).hasGameCaptainOnCourt();
    }

    @Override
    public int getGameCaptain(TeamType teamType, int setIndex) {
        int number = -1;

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            Indoor4x4TeamComposition indoorTeamComposition = (Indoor4x4TeamComposition) set.getTeamComposition(teamType);
            number = indoorTeamComposition.getGameCaptain();
        }

        return number;
    }

    @Override
    public boolean isGameCaptain(TeamType teamType, int number) {
        return getIndoorTeamComposition(teamType).isGameCaptain(number);
    }

    @Override
    public void setGameCaptain(TeamType teamType, int number) {
        getIndoorTeamComposition(teamType).setGameCaptain(number);
    }

    @Override
    public Set<Integer> getPossibleSecondaryCaptains(TeamType teamType) {
        return getIndoorTeamComposition(teamType).getPossibleSecondaryCaptains();
    }

    @Override
    public boolean hasRemainingSubstitutions(TeamType teamType) {
        return getIndoorTeamComposition(teamType).canSubstitute();
    }

    @Override
    public int countRemainingSubstitutions(TeamType teamType) {
        return getIndoorTeamComposition(teamType).countRemainingSubstitutions();
    }

    @Override
    public Set<Integer> filterSubstitutionsWithEvictedPlayersForCurrentSet(TeamType teamType,
                                                                           int evictedNumber,
                                                                           Set<Integer> possibleSubstitutions) {
        final Set<Integer> filteredSubstitutions = new HashSet<>(possibleSubstitutions);
        final Set<Integer> evictedPlayers = getEvictedPlayersForCurrentSet(teamType, true, true);

        filteredSubstitutions.removeIf(evictedPlayers::contains);

        return filteredSubstitutions;
    }

    @Override
    public int getWaitingMiddleBlocker(TeamType teamType) {
        return -1;
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType) {
        return getIndoorTeamComposition(teamType).isStartingLineupConfirmed();
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType, int setIndex) {
        return getIndoorTeamComposition(teamType, setIndex).isStartingLineupConfirmed();
    }

    @Override
    public CourtDto getStartingLineup(TeamType teamType, int setIndex) {
        CourtDto startingLineup = new CourtDto();

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            Indoor4x4TeamComposition indoorTeamComposition = (Indoor4x4TeamComposition) set.getTeamComposition(teamType);
            startingLineup = indoorTeamComposition.getStartingLineup();
        }

        return startingLineup;
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        PositionType positionType = null;

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            Indoor4x4TeamComposition indoorTeamComposition = (Indoor4x4TeamComposition) set.getTeamComposition(teamType);
            positionType = indoorTeamComposition.getPlayerPositionInStartingLineup(number);
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        int number = -1;

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            Indoor4x4TeamComposition indoorTeamComposition = (Indoor4x4TeamComposition) set.getTeamComposition(teamType);
            number = indoorTeamComposition.getPlayerAtPositionInStartingLineup(positionType);
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
    public Set<PlayerDto> getLiberos(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    public List<SubstitutionDto> getSubstitutions(TeamType teamType) {
        return getIndoorTeamComposition(teamType).getSubstitutionsCopy();
    }

    @Override
    public List<SubstitutionDto> getSubstitutions(TeamType teamType, int setIndex) {
        List<SubstitutionDto> substitutions = new ArrayList<>();

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            Indoor4x4TeamComposition indoorTeamComposition = (Indoor4x4TeamComposition) set.getTeamComposition(teamType);
            substitutions = indoorTeamComposition.getSubstitutionsCopy();
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
                final Set<Integer> possibleSubstitutions = getPossibleSubstitutions(teamType, positionType);
                final Set<Integer> filteredSubstitutions = filterSubstitutionsWithEvictedPlayersForCurrentSet(teamType, number,
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
}
