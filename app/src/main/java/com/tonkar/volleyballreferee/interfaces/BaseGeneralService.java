package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.business.rules.Rules;

public interface BaseGeneralService {

    GameType getKind();

    String geId();

    String getCreatedBy();

    long getCreatedAt();

    long getUpdatedAt();

    void setUpdatedAt(long updatedAt);

    long getScheduledAt();

    String getRefereedBy();

    String getRefereeName();

    GameStatus getMatchStatus();

    boolean isMatchCompleted();

    UsageType getUsage();

    void setUsage(UsageType usage);

    Rules getRules();

    String getLeagueId();

    void setLeagueId(String id);

    String getLeagueName();

    void setLeagueName(String name);

    String getDivisionName();

    void setDivisionName(String name);

    boolean isIndexed();

    void setIndexed(boolean indexed);
}
