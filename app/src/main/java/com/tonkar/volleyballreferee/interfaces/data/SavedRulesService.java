package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.rules.Rules;

import java.util.List;

public interface SavedRulesService {

    String SAVED_RULES_FILE = "device_saved_rules.json";

    void loadSavedRules();

    List<Rules> getSavedRules();

    Rules getSavedRules(String rulesName);

    void createRules();

    void editRules(String rulesName);

    Rules getCurrentRules();

    void saveCurrentRules();

    void deleteSavedRules(String rulesName);

    void deleteAllSavedRules();

}
