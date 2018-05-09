package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.rules.Rules;

public interface BaseGeneralService {

    UserId getUserId();

    GameType getGameType();

    long getGameDate();

    long getGameSchedule();

    GameStatus getMatchStatus();

    boolean isMatchCompleted();

    UsageType getUsageType();

    void setUsageType(UsageType usageType);

    Rules getRules();
}
