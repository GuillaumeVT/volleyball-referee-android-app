package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.interfaces.card.BasePenaltyCardService;
import com.tonkar.volleyballreferee.interfaces.score.BaseScoreService;
import com.tonkar.volleyballreferee.interfaces.BaseTimeService;
import com.tonkar.volleyballreferee.interfaces.timeout.BaseTimeoutService;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;

public interface RecordedGameService extends BaseScoreService, BaseIndoorTeamService, BaseTimeoutService, BasePenaltyCardService, BaseTimeService, WebGameService {

    boolean matchesFilter(String text);

}
