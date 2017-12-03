package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.rules.Rules;

public interface ScoreService extends BaseScoreService {

    void addScoreListener(ScoreListener listener);

    void removeScoreListener(ScoreListener listener);

    Rules getRules();

    boolean isMatchPoint();

    boolean isSetPoint();

    void addPoint(TeamType teamType);

    void removeLastPoint();

    TeamType getLeadingTeam();

    void swapServiceAtStart();

}
