package com.tonkar.volleyballreferee.interfaces.timeout;

import com.tonkar.volleyballreferee.api.ApiTimeout;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.List;

public interface BaseTimeoutService {

    int getRemainingTimeouts(TeamType teamType);

    int getRemainingTimeouts(TeamType teamType, int setIndex);

    List<ApiTimeout> getCalledTimeouts(TeamType teamType);

    List<ApiTimeout> getCalledTimeouts(TeamType teamType, int setIndex);

}
