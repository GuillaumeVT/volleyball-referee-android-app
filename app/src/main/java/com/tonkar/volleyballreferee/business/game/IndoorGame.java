package com.tonkar.volleyballreferee.business.game;

import android.util.Log;

import com.tonkar.volleyballreferee.business.team.Team;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.business.team.IndoorTeam;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.AbstractMap;
import java.util.List;

public class IndoorGame extends Game implements IndoorTeamService {

    IndoorGame(final Rules rules) {
        super(GameType.INDOOR, rules);
    }

    @Override
    protected Team createTeam(TeamType teamType) {
        return new IndoorTeam(teamType, getRules().getTeamSubstitutionsPerSet());
    }

    private IndoorTeam getIndoorTeam(TeamType teamType) {
        return (IndoorTeam) getTeam(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
            checkPosition1(teamType);

            final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

            // In indoor volley, the teams change sides after the 8th during the tie break
            if (isTieBreakSet() && leadingScore == 8 && currentSet().getPoints(TeamType.HOME) != currentSet().getPoints(TeamType.GUEST)) {
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
        }
    }

    @Override
    public void removeLastPoint() {
        super.removeLastPoint();

        checkPosition1(getServingTeam());

        final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

        // In indoor volley, the teams change sides after the 8th during the tie break
        if (isTieBreakSet() && leadingScore == 7) {
            swapTeams(ActionOriginType.APPLICATION);
        }
    }

    @Override
    protected void onNewSet() {
        // Both coaches must provide a team composition to the referee for each new set
        putAllPlayersOnBench(TeamType.HOME);
        putAllPlayersOnBench(TeamType.GUEST);
    }

    private void checkPosition1(final TeamType scoringTeam) {
        int number = getIndoorTeam(scoringTeam).checkPosition1Offence();
        if (number > 0)  {
            substitutePlayer(scoringTeam, number, PositionType.POSITION_1, ActionOriginType.APPLICATION);
        }

        TeamType defendingTeam = scoringTeam.other();
        number = getIndoorTeam(defendingTeam).checkPosition1Defence();
        if (number > 0)  {
            substitutePlayer(defendingTeam, number, PositionType.POSITION_1, ActionOriginType.APPLICATION);
        }
    }

    private void putAllPlayersOnBench(final TeamType teamType) {
        Log.i("VBR-Team", String.format("Put all players of %s team on bench", teamType.toString()));
        getIndoorTeam(teamType).putAllPlayersOnBench();
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        return getTeam(teamType).getNumberOfPlayers();
    }

    @Override
    public void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (getTeam(teamType).substitutePlayer(number, positionType)) {
            notifyPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    @Override
    public List<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType) {
        return getIndoorTeam(teamType).getPossibleSubstitutions(positionType);
    }

    @Override
    public void confirmStartingLineup() {
        getIndoorTeam(TeamType.HOME).confirmStartingLineup();
        getIndoorTeam(TeamType.GUEST).confirmStartingLineup();
    }

    @Override
    public boolean isStartingLineupConfirmed() {
        return getIndoorTeam(TeamType.HOME).isStartingLineupConfirmed() && getIndoorTeam(TeamType.GUEST).isStartingLineupConfirmed();
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return getIndoorTeam(teamType).getLiberoColor();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {
        getIndoorTeam(teamType).setLiberoColor(color);
    }

    @Override
    public void addLibero(TeamType teamType, int number) {
        getIndoorTeam(teamType).addLibero(number);
    }

    @Override
    public void removeLibero(TeamType teamType, int number) {
        getIndoorTeam(teamType).removeLibero(number);
    }

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return getIndoorTeam(teamType).isLibero(number);
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return getIndoorTeam(teamType).canAddLibero();
    }

    @Override
    public List<AbstractMap.SimpleEntry<Integer, Integer>> getSubstitutions(TeamType teamType) {
        return getIndoorTeam(teamType).getSubstitutions();
    }

    @Override
    public int getNumberOfSubstitutions(TeamType teamType) {
        return getIndoorTeam(teamType).getNumberOfSubstitutions();
    }
}
