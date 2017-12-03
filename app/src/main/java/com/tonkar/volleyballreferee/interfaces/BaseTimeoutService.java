package com.tonkar.volleyballreferee.interfaces;

public interface BaseTimeoutService {

    int getTimeouts(TeamType teamType);

    int getTimeouts(TeamType teamType, int setIndex);

}
