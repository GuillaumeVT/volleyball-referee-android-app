package com.tonkar.volleyballreferee.interfaces;

public interface TimeBasedGameService extends GameService, BaseTimeService {

    long getDuration();

    void setDuration(long duration);

    void start();

    void stop();

    boolean isMatchStarted();

    boolean isMatchRunning();

    boolean isMatchStopped();

}
