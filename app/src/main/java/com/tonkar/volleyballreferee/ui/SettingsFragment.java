package com.tonkar.volleyballreferee.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mPrefListener = (prefs, key) -> {
            if(key.equals(PrefUtils.PREF_NIGHT_MODE)) {
                PrefUtils.applyNightMode(getContext());
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mPrefListener != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefListener);
        }
    }

}
