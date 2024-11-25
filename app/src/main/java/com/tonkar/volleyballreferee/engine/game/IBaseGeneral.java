package com.tonkar.volleyballreferee.engine.game;

import com.tonkar.volleyballreferee.engine.api.model.SelectedLeagueDto;
import com.tonkar.volleyballreferee.engine.rules.Rules;

public interface IBaseGeneral {

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

    SelectedLeagueDto getLeague();

    long getStartTime();

    long getEndTime();

    String getReferee1Name();

    void setReferee1Name(String referee1Name);

    String getReferee2Name();

    void setReferee2Name(String referee2Name);

    String getScorerName();

    void setScorerName(String scorerName);
}
