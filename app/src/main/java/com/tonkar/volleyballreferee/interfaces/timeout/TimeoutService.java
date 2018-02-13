package com.tonkar.volleyballreferee.interfaces.timeout;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public interface TimeoutService extends BaseTimeoutService {

    void addTimeoutListener(TimeoutListener listener);

    void removeTimeoutListener(TimeoutListener listener);

    void callTimeout(TeamType teamType);
}
