package com.tonkar.volleyballreferee.engine.game.score;

import com.tonkar.volleyballreferee.engine.team.TeamType;

public interface ScoreListener {

    void onPointsUpdated(TeamType teamType, int newCount);

    void onSetsUpdated(TeamType teamType, int newCount);

    void onServiceSwapped(TeamType teamType, boolean isStart);

    void onSetStarted();

    void onSetCompleted();

    void onMatchCompleted(final TeamType winner);
}
