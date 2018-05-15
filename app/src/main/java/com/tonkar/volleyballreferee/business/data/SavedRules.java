package com.tonkar.volleyballreferee.business.data;

import android.content.Context;

import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.rules.Rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SavedRules implements SavedRulesService {

    private final Context     mContext;
    private final List<Rules> mSavedRulesList;
    private       Rules       mSavedRules;

    public SavedRules(Context context) {
        mContext = context;
        mSavedRulesList = new ArrayList<>();
    }

    @Override
    public void loadSavedRules() {
        mSavedRules = null;
        mSavedRulesList.clear();
        mSavedRulesList.addAll(JsonIOUtils.readSavedRules(mContext, SAVED_RULES_FILE));
    }

    @Override
    public List<Rules> getSavedRules() {
        List<Rules> list = new ArrayList<>();
        list.addAll(mSavedRulesList);
        return list;
    }

    @Override
    public Rules getSavedRules(String rulesName) {
        Rules matching = null;

        for (Rules savedRules : mSavedRulesList) {
            if (savedRules.getName().equals(rulesName)) {
                matching = savedRules;
            }
        }

        return matching;
    }

    @Override
    public void createRules() {
        mSavedRules = Rules.defaultUniversalRules();
        mSavedRules.setName("");
        mSavedRules.setDate(System.currentTimeMillis());
    }

    @Override
    public void editRules(String rulesName) {
        mSavedRules = getSavedRules(rulesName);
        mSavedRulesList.remove(mSavedRules);
    }

    @Override
    public Rules getCurrentRules() {
        return mSavedRules;
    }

    @Override
    public void saveCurrentRules() {
        mSavedRulesList.add(mSavedRules);
        JsonIOUtils.writeSavedRules(mContext, SAVED_RULES_FILE, mSavedRulesList);
        mSavedRules = null;
    }

    @Override
    public void deleteSavedRules(String rulesName) {
        for (Iterator<Rules> iterator = mSavedRulesList.iterator(); iterator.hasNext();) {
            Rules savedRules = iterator.next();
            if (savedRules.getName().equals(rulesName)) {
                iterator.remove();
            }
        }
        JsonIOUtils.writeSavedRules(mContext, SAVED_RULES_FILE, mSavedRulesList);
    }

    @Override
    public void deleteAllSavedRules() {
        mSavedRulesList.clear();
        JsonIOUtils.writeSavedRules(mContext, SAVED_RULES_FILE, mSavedRulesList);
    }

    @Override
    public void createAndSaveRulesFrom(Rules rules) {
        if (rules.getName().length() > 0
                && getSavedRules(rules.getName()) == null
                && !rules.getName().equals(Rules.officialBeachRules().getName())
                && !rules.getName().equals(Rules.officialIndoorRules().getName())
                && !rules.getName().equals(Rules.defaultIndoor4x4Rules().getName())
                && !rules.getName().equals(Rules.defaultUniversalRules().getName())) {
            createRules();
            getCurrentRules().setAll(rules);
            saveCurrentRules();
        }
    }
}
