package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.rules.Rules;

public interface GameService extends BaseGameService {

    void addGameListener(GameListener listener);

    void removeGameListener(GameListener listener);

    Rules getRules();

    boolean isGameCompleted();

    boolean isSetPoint();

    void addPoint(TeamType teamType);

    void removeLastPoint();

    TeamType getServingTeam();

    void swapServiceAtStart();

}
