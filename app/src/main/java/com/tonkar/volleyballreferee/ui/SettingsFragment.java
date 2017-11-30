package com.tonkar.volleyballreferee.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.tonkar.volleyballreferee.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
