package com.tonkar.volleyballreferee.interfaces;

import com.tonkar.volleyballreferee.api.ApiSelectedLeague;
import com.tonkar.volleyballreferee.business.rules.Rules;

public interface BaseGeneralService {

    GameType getKind();

    String getId();

    String getCreatedBy();

    long getCreatedAt();

    long getUpdatedAt();

    void setUpdatedAt(long updatedAt);

    long getScheduledAt();

    String getRefereedBy();

    void setRefereedBy(String refereedBy);

    String getRefereeName();

    void setRefereeName(String refereeName);

    GameStatus getMatchStatus();

    boolean isMatchCompleted();

    UsageType getUsage();

    void setUsage(UsageType usage);

    Rules getRules();

    ApiSelectedLeague getLeague();

    boolean isIndexed();

    void setIndexed(boolean indexed);
}
