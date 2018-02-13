package com.tonkar.volleyballreferee.interfaces.timeout;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public interface TimeoutListener {

    void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount);

    void onTimeout(TeamType teamType, int duration);

    void onTechnicalTimeout(int duration);

    void onGameInterval(int duration);
}
