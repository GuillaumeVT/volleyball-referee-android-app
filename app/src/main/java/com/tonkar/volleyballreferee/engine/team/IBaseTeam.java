package com.tonkar.volleyballreferee.engine.team;

import com.tonkar.volleyballreferee.engine.api.model.ApiCourt;
import com.tonkar.volleyballreferee.engine.api.model.ApiPlayer;
import com.tonkar.volleyballreferee.engine.api.model.ApiSubstitution;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.List;
import java.util.Set;

public interface IBaseTeam {

    String getTeamId(TeamType teamType);

    void setTeamId(TeamType teamType, String id);

    String getCreatedBy(TeamType teamType);

    void setCreatedBy(TeamType teamType, String createdBy);

    long getCreatedAt(TeamType teamType);

    void setCreatedAt(TeamType teamType, long createdAt);

    long getUpdatedAt(TeamType teamType);

    void setUpdatedAt(TeamType teamType, long updatedAt);

    GameType getTeamsKind();

    String getTeamName(TeamType teamType);

    int getTeamColor(TeamType teamType);

    void setTeamName(TeamType teamType, String name);

    void setTeamColor(TeamType teamType, int color);

    void addPlayer(TeamType teamType, int number);

    void removePlayer(TeamType teamType, int number);

    boolean hasPlayer(TeamType teamType, int number);

    int getNumberOfPlayers(TeamType teamType);

    Set<ApiPlayer> getPlayers(TeamType teamType);

    void setPlayerName(TeamType teamType, int number, String name);

    String getPlayerName(TeamType teamType, int number);

    GenderType getGender();

    GenderType getGender(TeamType teamType);

    void setGender(GenderType gender);

    void setGender(TeamType teamType, GenderType gender);

    int getExpectedNumberOfPlayersOnCourt();

    int getLiberoColor(TeamType teamType);

    void setLiberoColor(TeamType teamType, int color);

    void addLibero(TeamType teamType, int number);

    void removeLibero(TeamType teamType, int number);

    boolean isLibero(TeamType teamType, int number);

    boolean canAddLibero(TeamType teamType);

    Set<ApiPlayer> getLiberos(TeamType teamType);

    List<ApiSubstitution> getSubstitutions(TeamType teamType);

    List<ApiSubstitution> getSubstitutions(TeamType teamType, int setIndex);

    boolean isStartingLineupConfirmed(TeamType teamType);

    boolean isStartingLineupConfirmed(TeamType teamType, int setIndex);

    ApiCourt getStartingLineup(TeamType teamType, int setIndex);

    PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex);

    int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex);

    void setCaptain(TeamType teamType, int number);

    int getCaptain(TeamType teamType);

    Set<Integer> getPossibleCaptains(TeamType teamType);

    boolean isCaptain(TeamType teamType, int number);

    String getCoachName(TeamType teamType);

    void setCoachName(TeamType teamType, String name);

}
