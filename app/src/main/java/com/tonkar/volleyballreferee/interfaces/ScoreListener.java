package com.tonkar.volleyballreferee.interfaces;

public interface ScoreListener {

    void onPointsUpdated(TeamType teamType, int newCount);

    void onSetsUpdated(TeamType teamType, int newCount);

    void onServiceSwapped(TeamType teamType);

    void onSetCompleted();

    void onMatchCompleted(final TeamType winner);
}
