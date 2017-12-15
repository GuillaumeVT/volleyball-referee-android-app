package com.tonkar.volleyballreferee.interfaces;

public interface GameService extends ScoreService, TeamService, TimeoutService {

    UsageType getUsageType();

    void setUsageType(UsageType usageType);
}
