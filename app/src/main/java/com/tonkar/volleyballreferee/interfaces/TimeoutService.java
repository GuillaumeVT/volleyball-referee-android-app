package com.tonkar.volleyballreferee.interfaces;

public interface TimeoutService extends BaseTimeoutService {

    void addTimeoutListener(TimeoutListener listener);

    void removeTimeoutListener(TimeoutListener listener);

    void callTimeout(TeamType teamType);
}
