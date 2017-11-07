package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.rules.Rules;

public interface GameService extends BaseGameService {

    void addGameListener(GameListener listener);

    void removeGameListener(GameListener listener);

    Rules getRules();

    boolean isGameCompleted();

    void addPoint(TeamType teamType);

    void removePoint(TeamType teamType);

    TeamType getServingTeam();

    void swapServiceAtStart();

}
