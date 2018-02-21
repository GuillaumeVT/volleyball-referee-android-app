package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.interfaces.sanction.SanctionService;
import com.tonkar.volleyballreferee.interfaces.score.ScoreService;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutService;

public interface GameService extends ScoreService, TeamService, TimeoutService, SanctionService {

    boolean areNotificationsEnabled();

}
