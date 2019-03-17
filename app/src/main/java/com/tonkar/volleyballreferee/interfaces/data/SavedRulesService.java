package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.rules.Rules;

import java.util.List;

public interface SavedRulesService {

    boolean hasSavedRules();

    List<Rules> getSavedRules();

    Rules getSavedRules(String rulesName);

    Rules readRules(String json);

    String writeRules(Rules rules);

    Rules createRules();

    Rules createRules(GameType gameType);

    void saveRules(Rules rules);

    void deleteSavedRules(String rulesName);

    void deleteAllSavedRules();

    void createAndSaveRulesFrom(Rules rules);

    void syncRulesOnline();

    void syncRulesOnline(DataSynchronizationListener listener);
}
