package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.rules.Rules;

public interface BaseGeneralService {

    GameType getGameType();

    long getGameDate();

    long getGameSchedule();

    GameStatus getMatchStatus();

    boolean isMatchCompleted();

    UsageType getUsageType();

    void setUsageType(UsageType usageType);

    Rules getRules();

    String getLeagueName();

    void setLeagueName(String name);

    String getDivisionName();

    void setDivisionName(String name);
}
