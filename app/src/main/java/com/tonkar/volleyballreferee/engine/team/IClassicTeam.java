package com.tonkar.volleyballreferee.engine.team;

import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.Set;

public interface IClassicTeam extends ITeam {

    Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType);

    void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType);

    void confirmStartingLineup(TeamType teamType);

    boolean hasActingCaptainOnCourt(TeamType teamType);

    int getActingCaptain(TeamType teamType, int setIndex);

    void setActingCaptain(TeamType teamType, int number);

    boolean isActingCaptain(TeamType teamType, int number);

    Set<Integer> getPossibleActingCaptains(TeamType teamType);

    boolean hasRemainingSubstitutions(TeamType teamType);

    Set<Integer> filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(TeamType teamType, int excludedNumber, Set<Integer> possibleSubstitutions);

    int getWaitingMiddleBlocker(TeamType teamType);
}
