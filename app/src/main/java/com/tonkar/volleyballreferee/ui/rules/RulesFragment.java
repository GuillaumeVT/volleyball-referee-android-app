package com.tonkar.volleyballreferee.ui.rules;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.tonkar.volleyballreferee.R;

public class RulesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.rules);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
