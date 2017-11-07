package com.tonkar.volleyballreferee.interfaces;

public interface GameListener {

    void onPointsUpdated(TeamType teamType, int newCount);

    void onSetsUpdated(TeamType teamType, int newCount);

    void onServiceSwapped(TeamType teamType);

    void onSetCompleted();

    void onGameCompleted(final TeamType winner);
}
