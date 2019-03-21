package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.IndoorTeamComposition;
import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class IndoorGame extends Game implements IndoorTeamService {

    public IndoorGame(final long gameDate, final long gameSchedule, final Rules rules) {
        super(GameType.INDOOR, gameDate, gameSchedule, rules);
    }

    // For GSON Deserialization
    public IndoorGame() {
        this(0L, 0L, Rules.defaultUniversalRules());
    }

    @Override
    protected TeamDefinition createTeamDefinition(TeamType teamType) {
        return new IndoorTeamDefinition(GameType.INDOOR, teamType);
    }

    @Override
    protected Set createSet(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        return new IndoorSet(getRules(), pointsToWinSet, servingTeamAtStart, getTeamDefinition(TeamType.HOME), getTeamDefinition(TeamType.GUEST));
    }

    private IndoorTeamComposition getIndoorTeamComposition(TeamType teamType) {
        return (IndoorTeamComposition) currentSet().getTeamComposition(teamType);
    }

    private IndoorTeamComposition getIndoorTeamComposition(TeamType teamType, int setIndex) {
        return (IndoorTeamComposition) getSet(setIndex).getTeamComposition(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
            checkPosition1(teamType);

            final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

            // In indoor volley, the teams change sides after the 8th during the tie break
            if (isTieBreakSet() && leadingScore == 8 && currentSet().getPoints(TeamType.HOME) != currentSet().getPoints(TeamType.GUEST) && teamType.equals(currentSet().getLeadingTeam())) {
                swapTeams(ActionOriginType.APPLICATION);
            }

            // In indoor volley, there are two technical timeouts at 8 and 16 but not during tie break
            if (getRules().areTechnicalTimeoutsEnabled()
                    && !isTieBreakSet()
                    && currentSet().getLeadingTeam().equals(teamType)
                    && (leadingScore == 8 || leadingScore == 16)
                    && currentSet().getPoints(TeamType.HOME) != currentSet().getPoints(TeamType.GUEST)) {
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
        checkPosition1(newServingTeam);
        final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

        // In indoor volley, the teams change sides after the 8th during the tie break
        if (isTieBreakSet() && leadingScore == 7 && oldLeadingScore == 8) {
            swapTeams(ActionOriginType.APPLICATION);
        }

        // Specific custom rule
        if (oldServingTeam.equals(newServingTeam) && samePlayerHadServedNConsecutiveTimes(oldServingTeam, getPoints(oldServingTeam), getPointsLadder())) {
            rotateToPreviousPositions(oldServingTeam);
        }
    }

    private void checkPosition1(final TeamType scoringTeam) {
        int number = getIndoorTeamComposition(scoringTeam).checkPosition1Offence();
        if (number > 0)  {
            substitutePlayer(scoringTeam, number, PositionType.POSITION_1, ActionOriginType.APPLICATION);
        }

        TeamType defendingTeam = scoringTeam.other();
        number = getIndoorTeamComposition(defendingTeam).checkPosition1Defence();
        if (number > 0)  {
            substitutePlayer(defendingTeam, number, PositionType.POSITION_1, ActionOriginType.APPLICATION);
        }
    }

    @Override
    public void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (getIndoorTeamComposition(teamType).substitutePlayer(number, positionType, getPoints(TeamType.HOME), getPoints(TeamType.GUEST))) {
            notifyPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    @Override
    public java.util.Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType) {
        return getIndoorTeamComposition(teamType).getPossibleSubstitutions(positionType);
    }

    @Override
    public void confirmStartingLineup() {
        getIndoorTeamComposition(TeamType.HOME).confirmStartingLineup();
        getIndoorTeamComposition(TeamType.GUEST).confirmStartingLineup();
        notifyStartingLineupSubmitted();
    }

    @Override
    public boolean hasActingCaptainOnCourt(TeamType teamType) {
        return getIndoorTeamComposition(teamType).hasActingCaptainOnCourt();
    }

    @Override
    public int getActingCaptain(TeamType teamType, int setIndex) {
        int number = -1;

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            number = indoorTeamComposition.getActingCaptain();
        }

        return number;
    }

    @Override
    public void setActingCaptain(TeamType teamType, int number) {
        getIndoorTeamComposition(teamType).setActingCaptain(number);
    }

    @Override
    public boolean isActingCaptain(TeamType teamType, int number) {
        return getIndoorTeamComposition(teamType).isActingCaptain(number);
    }

    @Override
    public java.util.Set<Integer> getPossibleActingCaptains(TeamType teamType) {
        return getIndoorTeamComposition(teamType).getPossibleActingCaptains();
    }

    @Override
    public boolean hasRemainingSubstitutions(TeamType teamType) {
        return getIndoorTeamComposition(teamType).canSubstitute();
    }

    @Override
    public java.util.Set<Integer> filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(TeamType teamType, int excludedNumber, java.util.Set<Integer> possibleSubstitutions) {
        final java.util.Set<Integer> filteredSubstitutions = new HashSet<>(possibleSubstitutions);
        final java.util.Set<Integer> excludedNumbers = getExpulsedOrDisqualifiedPlayersForCurrentSet(teamType);

        for(Iterator<Integer> iterator = filteredSubstitutions.iterator(); iterator.hasNext();) {
            int possibleReplacement = iterator.next();
            if (excludedNumbers.contains(possibleReplacement)) {
                iterator.remove();
            } else if (!isLibero(teamType, excludedNumber) && isLibero(teamType, possibleReplacement)) {
                iterator.remove();
            }
        }

        return filteredSubstitutions;
    }

    @Override
    public boolean isStartingLineupConfirmed() {
        return getIndoorTeamComposition(TeamType.HOME).isStartingLineupConfirmed() && getIndoorTeamComposition(TeamType.GUEST).isStartingLineupConfirmed();
    }

    @Override
    public boolean isStartingLineupConfirmed(int setIndex) {
        return getIndoorTeamComposition(TeamType.HOME, setIndex).isStartingLineupConfirmed() && getIndoorTeamComposition(TeamType.GUEST, setIndex).isStartingLineupConfirmed();
    }

    @Override
    public java.util.Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex) {
        java.util.Set<Integer> players = new TreeSet<>();

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            players = indoorTeamComposition.getPlayersInStartingLineup();
        }

        return players;
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        PositionType positionType = null;

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            positionType = indoorTeamComposition.getPlayerPositionInStartingLineup(number);
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        int number = -1;

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            number = indoorTeamComposition.getPlayerAtPositionInStartingLineup(positionType);
        }

        return number;
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return getTeamDefinition(teamType).getLiberoColor();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {
        getTeamDefinition(teamType).setLiberoColor(color);
    }

    @Override
    public void addLibero(TeamType teamType, int number) {
        getTeamDefinition(teamType).addLibero(number);
    }

    @Override
    public void removeLibero(TeamType teamType, int number) {
        getTeamDefinition(teamType).removeLibero(number);
    }

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return getTeamDefinition(teamType).isLibero(number);
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return getTeamDefinition(teamType).canAddLibero();
    }

    @Override
    public java.util.Set<Integer> getLiberos(TeamType teamType) {
        return getTeamDefinition(teamType).getLiberos();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType) {
        return getIndoorTeamComposition(teamType).getSubstitutions();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        List<Substitution> substitutions = new ArrayList<>();

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            substitutions = indoorTeamComposition.getSubstitutions();
        }

        return substitutions;
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
    public void giveSanction(TeamType teamType, SanctionType sanctionType, int number) {
        super.giveSanction(teamType, sanctionType, number);

        if (number > 0 && (SanctionType.RED_EXPULSION.equals(sanctionType) || SanctionType.RED_DISQUALIFICATION.equals(sanctionType))) {
            // The player excluded for the set/match has to be legally replaced
            PositionType positionType = getPlayerPosition(teamType, number);

            if (!PositionType.BENCH.equals(positionType)) {
                final java.util.Set<Integer> possibleSubstitutions = getPossibleSubstitutions(teamType, positionType);
                final java.util.Set<Integer> filteredSubstitutions = filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(teamType, number, possibleSubstitutions);

                // If there is no possible legal substituion, the set is lost
                if (filteredSubstitutions.size() == 0) {
                    forceFinishSet(teamType.other());
                }
            }
        }

        if (SanctionType.RED_DISQUALIFICATION.equals(sanctionType) && !isMatchCompleted()) {
            // check that the team has enough players to continue the match
            java.util.Set<Integer> players = getTeamDefinition(teamType).getPlayers();

            // first remove the liberos

            for (int libero : getTeamDefinition(teamType).getLiberos()) {
                players.remove(libero);
            }

            // then remove the disqualified players

            for (Sanction sanction : getGivenSanctions(teamType)) {
                if (SanctionType.RED_DISQUALIFICATION.equals(sanction.getSanctionType())) {
                    players.remove(sanction.getPlayer());
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
    public void restoreTeams(RecordedGameService recordedGameService) {
        super.restoreTeams(recordedGameService);

        setLiberoColor(TeamType.HOME, recordedGameService.getLiberoColor(TeamType.HOME));

        for (int number : recordedGameService.getLiberos(TeamType.HOME))  {
            addPlayer(TeamType.HOME, number);
            addLibero(TeamType.HOME, number);
        }

        setCaptain(TeamType.HOME, recordedGameService.getCaptain(TeamType.HOME));

        setLiberoColor(TeamType.GUEST, recordedGameService.getLiberoColor(TeamType.GUEST));

        for (int number : recordedGameService.getLiberos(TeamType.GUEST))  {
            addPlayer(TeamType.GUEST, number);
            addLibero(TeamType.GUEST, number);
        }

        setCaptain(TeamType.GUEST, recordedGameService.getCaptain(TeamType.GUEST));
    }

    @Override
    public void restoreGame(RecordedGameService recordedGameService) {
        if (GameStatus.LIVE.equals(recordedGameService.getMatchStatus())) {
            startMatch();

            for (int setIndex = 0; setIndex < recordedGameService.getNumberOfSets(); setIndex++) {
                List<TeamType> pointsLadder = recordedGameService.getPointsLadder(setIndex);
                boolean isStartingLineupConfirmed = recordedGameService.isStartingLineupConfirmed();

                if (isStartingLineupConfirmed) {
                    java.util.Set<Integer> homePlayers = recordedGameService.getPlayersInStartingLineup(TeamType.HOME, setIndex);
                    for (int player : homePlayers) {
                        PositionType positionType = recordedGameService.getPlayerPositionInStartingLineup(TeamType.HOME, player, setIndex);
                        substitutePlayer(TeamType.HOME, player, positionType, ActionOriginType.USER);
                    }

                    java.util.Set<Integer> guestPlayers = recordedGameService.getPlayersInStartingLineup(TeamType.GUEST, setIndex);
                    for (int player : guestPlayers) {
                        PositionType positionType = recordedGameService.getPlayerPositionInStartingLineup(TeamType.GUEST, player, setIndex);
                        substitutePlayer(TeamType.GUEST, player, positionType, ActionOriginType.USER);
                    }

                    confirmStartingLineup();
                }

                getSet(setIndex).setServingTeamAtStart(recordedGameService.getFirstServingTeam(setIndex));

                for (int pointsIndex = 0; pointsIndex < pointsLadder.size(); pointsIndex++) {
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

                    List<Substitution> homeSubstitutions = recordedGameService.getSubstitutionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (Substitution substitution : homeSubstitutions) {
                        PositionType positionType = getPlayerPosition(TeamType.HOME, substitution.getPlayerOut(), setIndex);
                        substitutePlayer(TeamType.HOME, substitution.getPlayerIn(), positionType, ActionOriginType.USER);
                    }

                    List<Substitution> guestSubstitutions = recordedGameService.getSubstitutionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (Substitution substitution : guestSubstitutions) {
                        PositionType positionType = getPlayerPosition(TeamType.GUEST, substitution.getPlayerOut(), setIndex);
                        substitutePlayer(TeamType.GUEST, substitution.getPlayerIn(), positionType, ActionOriginType.USER);
                    }

                    List<Sanction> homeSanctions = recordedGameService.getSanctionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (Sanction sanction : homeSanctions) {
                        giveSanction(TeamType.HOME, sanction.getSanctionType(), sanction.getPlayer());
                    }

                    List<Sanction> guestSanctions = recordedGameService.getSanctionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (Sanction sanction : guestSanctions) {
                        giveSanction(TeamType.GUEST, sanction.getSanctionType(), sanction.getPlayer());
                    }

                    if (pointsIndex == pointsLadder.size() - 1) {
                        setActingCaptain(TeamType.HOME, recordedGameService.getActingCaptain(TeamType.HOME, setIndex));
                        setActingCaptain(TeamType.GUEST, recordedGameService.getActingCaptain(TeamType.GUEST, setIndex));
                    }

                    addPoint(pointsLadder.get(pointsIndex));
                }
            }
        }
    }
}
