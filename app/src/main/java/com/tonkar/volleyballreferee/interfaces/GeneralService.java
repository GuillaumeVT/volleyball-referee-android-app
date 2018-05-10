package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.rules.Rules;

public interface GeneralService extends BaseGeneralService {

    void setRules(Rules rules);

    void setGameDate(long gameDate);

    void setGameSchedule(long gameSchedule);

    void startMatch();
}
