package com.tonkar.volleyballreferee.engine.game.timeout;

import com.tonkar.volleyballreferee.engine.stored.api.ApiTimeout;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.List;

public interface IBaseTimeout {

    int getRemainingTimeouts(TeamType teamType);

    int getRemainingTimeouts(TeamType teamType, int setIndex);

    List<ApiTimeout> getCalledTimeouts(TeamType teamType);

    List<ApiTimeout> getCalledTimeouts(TeamType teamType, int setIndex);

}
