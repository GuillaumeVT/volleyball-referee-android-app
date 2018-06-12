package com.tonkar.volleyballreferee.interfaces.score;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public interface ScoreService extends BaseScoreService {

    void addScoreListener(ScoreListener listener);

    void removeScoreListener(ScoreListener listener);

    boolean isMatchPoint();

    boolean isSetPoint();

    void addPoint(TeamType teamType);

    void removeLastPoint();

    TeamType getLeadingTeam();

    void swapServiceAtStart();

}
