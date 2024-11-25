package com.tonkar.volleyballreferee.engine.game.timeout;

import com.tonkar.volleyballreferee.engine.api.model.TimeoutDto;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.List;

public interface IBaseTimeout {

    int countRemainingTimeouts(TeamType teamType);

    int countRemainingTimeouts(TeamType teamType, int setIndex);

    List<TimeoutDto> getCalledTimeouts(TeamType teamType);

    List<TimeoutDto> getCalledTimeouts(TeamType teamType, int setIndex);

}
