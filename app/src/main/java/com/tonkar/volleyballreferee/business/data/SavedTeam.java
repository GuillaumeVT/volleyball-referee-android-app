package com.tonkar.volleyballreferee.business.data;

import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SavedTeam implements BaseIndoorTeamService {

    private IndoorTeamDefinition mIndoorTeamDefinition;

    public SavedTeam(IndoorTeamDefinition teamDefinition) {
        mIndoorTeamDefinition = teamDefinition;
    }

    public IndoorTeamDefinition getIndoorTeamDefinition() {
        return mIndoorTeamDefinition;
    }

    @Override
    public String getLeagueName() {
        return "";
    }

    @Override
    public void setLeagueName(String name) {}

    @Override
    public String getTeamName(TeamType teamType) {
        return mIndoorTeamDefinition.getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return mIndoorTeamDefinition.getColor();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {
        mIndoorTeamDefinition.setName(name);
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        mIndoorTeamDefinition.setColor(color);
    }

    @Override
    public void addPlayer(TeamType teamType, int number) {
        mIndoorTeamDefinition.addPlayer(number);
    }

    @Override
    public void removePlayer(TeamType teamType, int number) {
        mIndoorTeamDefinition.removePlayer(number);
    }

    @Override
    public boolean hasPlayer(TeamType teamType, int number) {
        return mIndoorTeamDefinition.hasPlayer(number);
    }

    @Override
    public int getNumberOfPlayers(TeamType teamType) {
        return mIndoorTeamDefinition.getNumberOfPlayers();
    }

    @Override
    public Set<Integer> getPlayers(TeamType teamType) {
        return mIndoorTeamDefinition.getPlayers();
    }

    @Override
    public GenderType getGenderType() {
        return mIndoorTeamDefinition.getGenderType();
    }

    @Override
    public GenderType getGenderType(TeamType teamType) {
        return mIndoorTeamDefinition.getGenderType();
    }

    @Override
    public void setGenderType(GenderType genderType) {
        mIndoorTeamDefinition.setGenderType(genderType);
    }

    @Override
    public void setGenderType(TeamType teamType, GenderType genderType) {
        mIndoorTeamDefinition.setGenderType(genderType);
    }

    @Override
    public void initTeams() {}

    @Override
    public int getLiberoColor(TeamType teamType) {
        return mIndoorTeamDefinition.getLiberoColor();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {
        mIndoorTeamDefinition.setLiberoColor(color);
    }

    @Override
    public void addLibero(TeamType teamType, int number) {
        mIndoorTeamDefinition.addLibero(number);
    }

    @Override
    public void removeLibero(TeamType teamType, int number) {
        mIndoorTeamDefinition.removeLibero(number);
    }

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return mIndoorTeamDefinition.isLibero(number);
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return mIndoorTeamDefinition.canAddLibero();
    }

    @Override
    public Set<Integer> getLiberos(TeamType teamType) {
        return mIndoorTeamDefinition.getLiberos();
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
        mIndoorTeamDefinition.setCaptain(number);
    }

    @Override
    public int getCaptain(TeamType teamType) {
        return mIndoorTeamDefinition.getCaptain();
    }

    @Override
    public Set<Integer> getPossibleCaptains(TeamType teamType) {
        return mIndoorTeamDefinition.getPossibleCaptains();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return mIndoorTeamDefinition.isCaptain(number);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof SavedTeam) {
            SavedTeam other = (SavedTeam) obj;
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
