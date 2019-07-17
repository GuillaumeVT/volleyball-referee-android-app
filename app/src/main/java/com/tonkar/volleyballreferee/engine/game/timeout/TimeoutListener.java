package com.tonkar.volleyballreferee.engine.game.timeout;

import com.tonkar.volleyballreferee.engine.team.TeamType;

public interface TimeoutListener {

    void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount);

    void onTimeout(TeamType teamType, int duration);

    void onTechnicalTimeout(int duration);

    void onGameInterval(int duration);
}
