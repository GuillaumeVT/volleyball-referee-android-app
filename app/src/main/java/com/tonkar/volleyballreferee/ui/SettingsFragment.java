package com.tonkar.volleyballreferee.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
