package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.rules.Rules;

import java.util.List;

public interface SavedRulesService {

    void migrateSavedRules();

    boolean hasSavedRules();

    List<Rules> getSavedRules();

    Rules getSavedRules(String rulesName);

    void createRules();

    void createRules(GameType gameType);

    void editRules(String rulesName);

    Rules getCurrentRules();

    void saveCurrentRules();

    void cancelCurrentRules();

    void deleteSavedRules(String rulesName);

    void deleteAllSavedRules();

    void createAndSaveRulesFrom(Rules rules);

    void syncRulesOnline();

    void syncRulesOnline(DataSynchronizationListener listener);
}
