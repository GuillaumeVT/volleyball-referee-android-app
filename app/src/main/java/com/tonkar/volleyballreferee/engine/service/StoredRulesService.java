package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;

import java.util.*;

public interface StoredRulesService {

    List<ApiRulesSummary> listRules();

    List<ApiRulesSummary> listRules(GameType kind);

    ApiRules getRules(String id);

    ApiRules getRules(GameType kind, String rulesName);

    Rules createRules(GameType kind);

    void saveRules(Rules rules, boolean create);

    void deleteRules(String id);

    void deleteRules(Set<String> ids, DataSynchronizationListener listener);

    void createAndSaveRulesFrom(Rules rules);

    void syncRules();

    void syncRules(DataSynchronizationListener listener);
}