package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface BaseTimeoutService {

    int getRemainingTimeouts(TeamType teamType);

    int getRemainingTimeouts(TeamType teamType, int setIndex);

    List<Timeout> getCalledTimeouts(TeamType teamType);

    List<Timeout> getCalledTimeouts(TeamType teamType, int setIndex);

}
