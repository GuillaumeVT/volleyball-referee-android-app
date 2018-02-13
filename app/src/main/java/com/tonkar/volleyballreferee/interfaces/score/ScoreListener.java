package com.tonkar.volleyballreferee.interfaces.score;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public interface ScoreListener {

    void onPointsUpdated(TeamType teamType, int newCount);

    void onSetsUpdated(TeamType teamType, int newCount);

    void onServiceSwapped(TeamType teamType);

    void onSetStarted();

    void onSetCompleted();

    void onMatchCompleted(final TeamType winner);
}
