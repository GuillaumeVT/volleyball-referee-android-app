package com.tonkar.volleyballreferee.engine.game.score;

import com.tonkar.volleyballreferee.engine.team.TeamType;

public interface IScore extends IBaseScore {

    void addScoreListener(ScoreListener listener);

    void removeScoreListener(ScoreListener listener);

    boolean isMatchPoint();

    boolean isSetPoint();

    void addPoint(TeamType teamType);

    TeamType getLeadingTeam();

    void swapServiceAtStart();

}
