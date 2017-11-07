package com.tonkar.volleyballreferee.interfaces;

public interface TimeoutListener {

    void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount);

    void onTimeout(int duration);
}
