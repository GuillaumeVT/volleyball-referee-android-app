package com.tonkar.volleyballreferee.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences.OnSharedPreferenceChangeListener mRefereeNameListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        updateRefereeName(sharedPreferences);

        mRefereeNameListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equals(PrefUtils.PREF_REFEREE_NAME)) {
                    updateRefereeName(prefs);
                }
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(mRefereeNameListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mRefereeNameListener != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(mRefereeNameListener);
        }
    }

    private void updateRefereeName(SharedPreferences sharedPreferences) {
        final String refereeName = PrefUtils.getPrefRefereeName(sharedPreferences);
        final EditTextPreference editTextPreference = (EditTextPreference) findPreference(PrefUtils.PREF_REFEREE_NAME);
        editTextPreference.setSummary(refereeName);
    }
}
