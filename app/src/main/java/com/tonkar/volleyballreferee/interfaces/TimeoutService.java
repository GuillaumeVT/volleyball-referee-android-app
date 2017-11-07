package com.tonkar.volleyballreferee.interfaces;

public interface TimeoutService {

    void addTimeoutListener(TimeoutListener listener);

    void removeTimeoutListener(TimeoutListener listener);

    int getTimeouts(TeamType teamType);

    void callTimeout(TeamType teamType);
}
