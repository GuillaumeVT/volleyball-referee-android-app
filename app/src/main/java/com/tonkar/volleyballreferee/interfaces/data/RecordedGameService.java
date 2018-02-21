package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.interfaces.sanction.BaseSanctionService;
import com.tonkar.volleyballreferee.interfaces.score.BaseScoreService;
import com.tonkar.volleyballreferee.interfaces.BaseTimeService;
import com.tonkar.volleyballreferee.interfaces.timeout.BaseTimeoutService;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;

public interface RecordedGameService extends BaseScoreService, BaseIndoorTeamService, BaseTimeoutService, BaseSanctionService, BaseTimeService, WebGameService {

    boolean matchesFilter(String text);

}
