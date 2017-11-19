package com.tonkar.volleyballreferee.interfaces;

public interface TimeoutListener {

    void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount);

    void onTimeout(TeamType teamType, int duration);

    void onTechnicalTimeout(int duration);

    void onGameInterval(int duration);
}
