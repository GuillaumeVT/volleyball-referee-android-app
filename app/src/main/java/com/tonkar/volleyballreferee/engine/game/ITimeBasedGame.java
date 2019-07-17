package com.tonkar.volleyballreferee.engine.game;

public interface ITimeBasedGame extends IGame, IBaseTime {

    long getDuration();

    void setDuration(long duration);

    void start();

    void stop();

    boolean isMatchStarted();

    boolean isMatchRunning();

    boolean isMatchStopped();

}
