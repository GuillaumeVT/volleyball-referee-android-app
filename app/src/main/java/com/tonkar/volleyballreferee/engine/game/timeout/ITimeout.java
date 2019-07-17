package com.tonkar.volleyballreferee.engine.game.timeout;

import com.tonkar.volleyballreferee.engine.team.TeamType;

public interface ITimeout extends IBaseTimeout {

    void addTimeoutListener(TimeoutListener listener);

    void removeTimeoutListener(TimeoutListener listener);

    void callTimeout(TeamType teamType);
}
