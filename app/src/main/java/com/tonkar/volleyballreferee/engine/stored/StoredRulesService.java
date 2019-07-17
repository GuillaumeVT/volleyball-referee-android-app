package com.tonkar.volleyballreferee.engine.stored;

import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRulesSummary;

import java.util.List;

public interface StoredRulesService {

    boolean hasRules();

    List<ApiRulesSummary> listRules();

    List<ApiRulesSummary> listRules(GameType kind);

    ApiRules getRules(String id);

    ApiRules getRules(GameType kind, String rulesName);

    ApiRules readRules(String json);

    String writeRules(ApiRules rules);

    Rules createRules(GameType kind);

    void saveRules(Rules rules, boolean create);

    void deleteRules(String id);

    void deleteAllRules();

    void createAndSaveRulesFrom(Rules rules);

    void syncRules();

    void syncRules(DataSynchronizationListener listener);
}
