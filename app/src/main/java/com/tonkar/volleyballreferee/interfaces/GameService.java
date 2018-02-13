package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.interfaces.card.PenaltyCardService;
import com.tonkar.volleyballreferee.interfaces.score.ScoreService;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutService;

public interface GameService extends ScoreService, TeamService, TimeoutService, PenaltyCardService {

    boolean areNotificationsEnabled();

}
