package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.*;

public class WrappedTeam implements IBaseTeam {

    private TeamDefinition mTeamDefinition;

    public WrappedTeam(TeamDefinition teamDefinition) {
        mTeamDefinition = teamDefinition;
    }

    public TeamDefinition getTeamDefinition() {
        return mTeamDefinition;
    }

    @Override
    public String getTeamId(TeamType teamType) {
        return mTeamDefinition.getId();
    }

    @Override
    public void setTeamId(TeamType teamType, String id) {
        mTeamDefinition.setId(id);
    }

    @Override
    public String getCreatedBy(TeamType teamType) {
        return mTeamDefinition.getCreatedBy();
    }

    @Override
    public void setCreatedBy(TeamType teamType, String userId) {
        mTeamDefinition.setCreatedBy(userId);
    }

    @Override
    public long getCreatedAt(TeamType teamType) {
        return mTeamDefinition.getCreatedAt();
    }

    @Override
    public void setCreatedAt(TeamType teamType, long createdAt) {
        mTeamDefinition.setCreatedAt(createdAt);
    }

    @Override
    public long getUpdatedAt(TeamType teamType) {
        return mTeamDefinition.getUpdatedAt();
    }

    @Override
    public void setUpdatedAt(TeamType teamType, long updatedAt) {
        mTeamDefinition.setUpdatedAt(updatedAt);
    }

    @Override
    public GameType getTeamsKind() {
        return mTeamDefinition.getKind();
    }

    @Override
    public String getTeamName(TeamType teamType) {
        return mTeamDefinition.getName();
    }

    @Override
    public int getTeamColor(TeamType teamType) {
        return mTeamDefinition.getColorInt();
    }

    @Override
    public void setTeamName(TeamType teamType, String name) {
        mTeamDefinition.setName(name);
    }

    @Override
    public void setTeamColor(TeamType teamType, int color) {
        mTeamDefinition.setColorInt(color);
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
    public Set<PlayerDto> getPlayers(TeamType teamType) {
        return new TreeSet<>(mTeamDefinition.getPlayers());
    }

    @Override
    public void setPlayerName(TeamType teamType, int number, String name) {
        mTeamDefinition.setPlayerName(number, name);
    }

    @Override
    public String getPlayerName(TeamType teamType, int number) {
        return mTeamDefinition.getPlayerName(number);
    }

    @Override
    public GenderType getGender() {
        return mTeamDefinition.getGender();
    }

    @Override
    public GenderType getGender(TeamType teamType) {
        return mTeamDefinition.getGender();
    }

    @Override
    public void setGender(GenderType gender) {
        mTeamDefinition.setGender(gender);
    }

    @Override
    public void setGender(TeamType teamType, GenderType gender) {
        mTeamDefinition.setGender(gender);
    }

    @Override
    public int getExpectedNumberOfPlayersOnCourt() {
        return mTeamDefinition.getExpectedNumberOfPlayersOnCourt();
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return mTeamDefinition.getLiberoColorInt();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {
        mTeamDefinition.setLiberoColorInt(color);
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
    public Set<PlayerDto> getLiberos(TeamType teamType) {
        return new TreeSet<>(mTeamDefinition.getLiberos());
    }

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
        return false;
    }

    @Override
    public boolean isStartingLineupConfirmed(TeamType teamType, int setIndex) {
        return false;
    }

    @Override
    public CourtDto getStartingLineup(TeamType teamType, int setIndex) {
        return null;
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
    public String getCoachName(TeamType teamType) {
        return mTeamDefinition.getCoach();
    }

    @Override
    public void setCoachName(TeamType teamType, String name) {
        mTeamDefinition.setCoach(name);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj == this) {
            result = true;
        } else if (obj instanceof WrappedTeam other) {
            result = this.getTeamName(null).equals(other.getTeamName(null)) && (this.getTeamColor(null) == other.getTeamColor(
                    null)) && (this.getLiberoColor(null) == other.getLiberoColor(null)) && (this.getCaptain(null) == other.getCaptain(
                    null)) && (this.getGender().equals(other.getGender())) && (this
                    .getPlayers(null)
                    .equals(other.getPlayers(null))) && (this.getLiberos(null).equals(other.getLiberos(null)));
        }

        return result;
    }
}
