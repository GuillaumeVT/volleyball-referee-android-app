package com.tonkar.volleyballreferee.engine.game;

import android.graphics.Color;

import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.set.SnowSet;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.engine.stored.api.ApiCourt;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPlayer;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSubstitution;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTimeout;
import com.tonkar.volleyballreferee.engine.team.IClassicTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.SnowTeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.SnowTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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
        return new SnowSet(getRules(), pointsToWinSet, servingTeamAtStart, getTeamDefinition(TeamType.HOME), getTeamDefinition(TeamType.GUEST));
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
        if (getSnowTeamComposition(teamType).substitutePlayer(number, positionType, getPoints(TeamType.HOME), getPoints(TeamType.GUEST))) {
            notifyPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    @Override
    protected void undoSubstitution(TeamType teamType, ApiSubstitution substitution) {
        java.util.Set<Integer> expulsedOrDisqualifiedPlayers = getExpulsedOrDisqualifiedPlayersForCurrentSet(teamType);
        if (!expulsedOrDisqualifiedPlayers.contains(substitution.getPlayerIn())
                && !expulsedOrDisqualifiedPlayers.contains(substitution.getPlayerOut())
                && getSnowTeamComposition(teamType).undoSubstitution(substitution)) {
            notifyPlayerChanged(teamType, substitution.getPlayerOut(), getSnowTeamComposition(teamType).getPlayerPosition(substitution.getPlayerOut()), ActionOriginType.APPLICATION);
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
    public boolean hasActingCaptainOnCourt(TeamType teamType) {
        return getSnowTeamComposition(teamType).hasActingCaptainOnCourt();
    }

    @Override
    public int getActingCaptain(TeamType teamType, int setIndex) {
        int number = -1;

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            SnowTeamComposition snowTeamComposition = (SnowTeamComposition) set.getTeamComposition(teamType);
            number = snowTeamComposition.getActingCaptain();
        }

        return number;
    }

    @Override
    public void setActingCaptain(TeamType teamType, int number) {
        getSnowTeamComposition(teamType).setActingCaptain(number);
    }

    @Override
    public boolean isActingCaptain(TeamType teamType, int number) {
        return getSnowTeamComposition(teamType).isActingCaptain(number);
    }

    @Override
    public java.util.Set<Integer> getPossibleActingCaptains(TeamType teamType) {
        return getSnowTeamComposition(teamType).getPossibleActingCaptains();
    }

    @Override
    public boolean hasRemainingSubstitutions(TeamType teamType) {
        return getSnowTeamComposition(teamType).canSubstitute();
    }

    @Override
    public java.util.Set<Integer> filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(TeamType teamType, int excludedNumber, java.util.Set<Integer> possibleSubstitutions) {
        final java.util.Set<Integer> filteredSubstitutions = new HashSet<>(possibleSubstitutions);
        final java.util.Set<Integer> excludedNumbers = getExpulsedOrDisqualifiedPlayersForCurrentSet(teamType);

        for (Iterator<Integer> iterator = filteredSubstitutions.iterator(); iterator.hasNext();) {
            int possibleReplacement = iterator.next();
            if (excludedNumbers.contains(possibleReplacement)) {
                iterator.remove();
            }
        }

        return filteredSubstitutions;
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
    public ApiCourt getStartingLineup(TeamType teamType, int setIndex) {
        ApiCourt startingLineup = new ApiCourt();

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
    public java.util.Set<ApiPlayer> getLiberos(TeamType teamType) {
        return new HashSet<>();
    }

    @Override
    public List<ApiSubstitution> getSubstitutions(TeamType teamType) {
        return getSnowTeamComposition(teamType).getSubstitutions();
    }

    @Override
    public List<ApiSubstitution> getSubstitutions(TeamType teamType, int setIndex) {
        List<ApiSubstitution> substitutions = new ArrayList<>();

        com.tonkar.volleyballreferee.engine.game.set.Set set = getSet(setIndex);

        if (set != null) {
            SnowTeamComposition snowTeamComposition = (SnowTeamComposition) set.getTeamComposition(teamType);
            substitutions = snowTeamComposition.getSubstitutions();
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

        if (ApiSanction.isPlayer(number) && (sanctionType.isMisconductExpulsionCard() || sanctionType.isMisconductDisqualificationCard())) {
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

        if (sanctionType.isMisconductDisqualificationCard() && !isMatchCompleted()) {
            // check that the team has enough players to continue the match
            List<ApiPlayer> players = getTeamDefinition(teamType).getPlayers();

            // Remove the disqualified players

            for (ApiSanction sanction : getAllSanctions(teamType)) {
                if (sanction.getCard().isMisconductDisqualificationCard()) {
                    players.remove(new ApiPlayer(sanction.getNum()));
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
                    ApiCourt homeStartingLineup = storedGame.getStartingLineup(TeamType.HOME, setIndex);

                    for (PositionType position : PositionType.listPositions(getKind())) {
                        substitutePlayer(TeamType.HOME, homeStartingLineup.getPlayerAt(position), position, ActionOriginType.USER);
                    }

                    confirmStartingLineup(TeamType.HOME);
                }

                if (storedGame.isStartingLineupConfirmed(TeamType.GUEST)) {
                    ApiCourt guestStartingLineup = storedGame.getStartingLineup(TeamType.GUEST, setIndex);

                    for (PositionType position : PositionType.listPositions(getKind())) {
                        substitutePlayer(TeamType.GUEST, guestStartingLineup.getPlayerAt(position), position, ActionOriginType.USER);
                    }

                    confirmStartingLineup(TeamType.GUEST);
                }

                getSet(setIndex).setServingTeamAtStart(storedGame.getFirstServingTeam(setIndex));

                for (int pointsIndex = 0; pointsIndex < pointsLadder.size(); pointsIndex++) {
                    int homePoints = getPoints(TeamType.HOME, setIndex);
                    int guestPoints = getPoints(TeamType.GUEST, setIndex);

                    List<ApiTimeout> homeTimeouts = storedGame.getTimeoutsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (ApiTimeout timeout : homeTimeouts) {
                        callTimeout(TeamType.HOME);
                    }

                    List<ApiTimeout> guestTimeouts = storedGame.getTimeoutsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (ApiTimeout timeout : guestTimeouts) {
                        callTimeout(TeamType.GUEST);
                    }

                    List<ApiSubstitution> homeSubstitutions = storedGame.getSubstitutionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (ApiSubstitution substitution : homeSubstitutions) {
                        PositionType positionType = getPlayerPosition(TeamType.HOME, substitution.getPlayerOut(), setIndex);
                        substitutePlayer(TeamType.HOME, substitution.getPlayerIn(), positionType, ActionOriginType.USER);
                    }

                    List<ApiSubstitution> guestSubstitutions = storedGame.getSubstitutionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (ApiSubstitution substitution : guestSubstitutions) {
                        PositionType positionType = getPlayerPosition(TeamType.GUEST, substitution.getPlayerOut(), setIndex);
                        substitutePlayer(TeamType.GUEST, substitution.getPlayerIn(), positionType, ActionOriginType.USER);
                    }

                    List<ApiSanction> homeSanctions = storedGame.getSanctionsIfExist(TeamType.HOME, setIndex, homePoints, guestPoints);
                    for (ApiSanction sanction : homeSanctions) {
                        giveSanction(TeamType.HOME, sanction.getCard(), sanction.getNum());
                    }

                    List<ApiSanction> guestSanctions = storedGame.getSanctionsIfExist(TeamType.GUEST, setIndex, homePoints, guestPoints);
                    for (ApiSanction sanction : guestSanctions) {
                        giveSanction(TeamType.GUEST, sanction.getCard(), sanction.getNum());
                    }

                    if (pointsIndex == pointsLadder.size() - 1) {
                        setActingCaptain(TeamType.HOME, storedGame.getActingCaptain(TeamType.HOME, setIndex));
                        setActingCaptain(TeamType.GUEST, storedGame.getActingCaptain(TeamType.GUEST, setIndex));
                    }

                    addPoint(pointsLadder.get(pointsIndex));
                }
            }
        }
    }

    @Override
    public java.util.Set<SanctionType> getPossibleMisconductSanctions(TeamType teamType, int number) {
        boolean teamHasReachedPenalty = false;

        for (ApiSanction sanction : getAllSanctions(teamType)) {
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

        for (ApiSanction sanction : getPlayerSanctions(teamType, number)) {
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
