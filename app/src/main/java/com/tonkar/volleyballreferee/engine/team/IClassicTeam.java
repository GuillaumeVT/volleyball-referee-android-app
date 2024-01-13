package com.tonkar.volleyballreferee.engine.team;

import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.util.Set;

public interface IClassicTeam extends ITeam {

    Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType);

    void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType);

    void confirmStartingLineup(TeamType teamType);

    boolean hasGameCaptainOnCourt(TeamType teamType);

    int getGameCaptain(TeamType teamType, int setIndex);

    boolean isGameCaptain(TeamType teamType, int number);

    void setGameCaptain(TeamType teamType, int number);

    Set<Integer> getPossibleSecondaryCaptains(TeamType teamType);

    boolean hasRemainingSubstitutions(TeamType teamType);

    int countRemainingSubstitutions(TeamType teamType);

    Set<Integer> filterSubstitutionsWithEvictedPlayersForCurrentSet(TeamType teamType,
                                                                    int evictedNumber,
                                                                    Set<Integer> possibleSubstitutions);

    int getWaitingMiddleBlocker(TeamType teamType);
}
