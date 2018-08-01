package com.tonkar.volleyballreferee.business.data;

import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class WrappedTeam implements BaseTeamService {

    private TeamDefinition mTeamDefinition;

    public WrappedTeam(TeamDefinition teamDefinition) {
        mTeamDefinition = teamDefinition;
    }

    public TeamDefinition getTeamDefinition() {
        return mTeamDefinition;
    }

    @Override
    public GameType getTeamsKind() {
        return mTeamDefinition.getGameType();
    }

    @Override
    public String getTeamName(TeamType teamType) {
        return mTeamDefinition.getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return mTeamDefinition.getColor();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {
        mTeamDefinition.setName(name);
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        mTeamDefinition.setColor(color);
    }

    @Override
    public void addPlayer(TeamType teamType, int number) {
        mTeamDefinition.addPlayer(number);
    }

    @Override
    public void removePlayer(TeamType teamType, int number) {
        mTeamDefinition.removePlayer(number);
    }

    @Override
    public boolean hasPlayer(TeamType teamType, int number) {
        return mTeamDefinition.hasPlayer(number);
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        return mTeamDefinition.getNumberOfPlayers();
    }

    @Override
    public Set<Integer> getPlayers(TeamType teamType) {
        return mTeamDefinition.getPlayers();
    }

    @Override
    public GenderType getGenderType() {
        return mTeamDefinition.getGenderType();
    }

    @Override
    public GenderType getGenderType(TeamType teamType) {
        return mTeamDefinition.getGenderType();
    }

    @Override
    public void setGenderType(GenderType genderType) {
        mTeamDefinition.setGenderType(genderType);
    }

    @Override
    public void setGenderType(TeamType teamType, GenderType genderType) {
        mTeamDefinition.setGenderType(genderType);
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return mTeamDefinition.getExpectedNumberOfPlayersOnCourt();
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return mTeamDefinition.getLiberoColor();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {
        mTeamDefinition.setLiberoColor(color);
    }

    @Override
    public void addLibero(TeamType teamType, int number) {
        mTeamDefinition.addLibero(number);
    }

    @Override
    public void removeLibero(TeamType teamType, int number) {
        mTeamDefinition.removeLibero(number);
    }

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return mTeamDefinition.isLibero(number);
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return mTeamDefinition.canAddLibero();
    }

    @Override
    public Set<Integer> getLiberos(TeamType teamType) {
        return mTeamDefinition.getLiberos();
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
        return false;
    }

    @Override
    public boolean isStartingLineupConfirmed(int setIndex) {
        return false;
    }

    @Override
    public Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex) {
        return new TreeSet<>();
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        return PositionType.BENCH;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        return -1;
    }

    @Override
    public void setCaptain(TeamType teamType, int number) {
        mTeamDefinition.setCaptain(number);
    }

    @Override
    public int getCaptain(TeamType teamType) {
        return mTeamDefinition.getCaptain();
    }

    @Override
    public Set<Integer> getPossibleCaptains(TeamType teamType) {
        return mTeamDefinition.getPossibleCaptains();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return mTeamDefinition.isCaptain(number);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof WrappedTeam) {
            WrappedTeam other = (WrappedTeam) obj;
            result = this.getTeamName(null).equals(other.getTeamName(null))
                    && (this.getTeamColor(null) == other.getTeamColor(null))
                    && (this.getLiberoColor(null) == other.getLiberoColor(null))
                    && (this.getCaptain(null) == other.getCaptain(null))
                    && (this.getGenderType().equals(other.getGenderType()))
                    && (this.getPlayers(null).equals(other.getPlayers(null)))
                    && (this.getLiberos(null).equals(other.getLiberos(null)));
        }

        return result;
    }
}
