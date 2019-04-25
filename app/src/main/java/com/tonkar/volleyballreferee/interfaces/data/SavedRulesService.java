package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiRules;
import com.tonkar.volleyballreferee.api.ApiRulesDescription;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.business.rules.Rules;

import java.util.List;

public interface SavedRulesService {

    boolean hasRules();

    List<ApiRulesDescription> listRules();

    List<ApiRulesDescription> listRules(GameType kind);

    List<String> listRulesNames(GameType kind);

    ApiRules getRules(String id);

    ApiRules getRules(GameType kind, String rulesName);

    ApiRules readRules(String json);

    String writeRules(ApiRules rules);

    Rules createRules(GameType kind);

    void saveRules(Rules rules);

    void deleteRules(String id);

    void deleteAllRules();

    void createAndSaveRulesFrom(Rules rules);

    void syncRules();

    void syncRules(DataSynchronizationListener listener);
}
