package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.rules.Rules;

public interface GeneralService extends BaseGeneralService {

    void startMatch(Rules rules, long gameDate, long gameSchedule);
}
