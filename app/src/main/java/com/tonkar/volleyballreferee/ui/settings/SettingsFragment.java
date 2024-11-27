package com.tonkar.volleyballreferee.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.*;
import androidx.preference.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    public SettingsFragment() {}

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        mPrefListener = (prefs, key) -> {
            if (PrefUtils.PREF_NIGHT_MODE.equals(key)) {
                PrefUtils.applyNightMode(requireContext());
            }

            if (PrefUtils.PREF_SERVER_URL.equals(key)) {
                if (PrefUtils.hasServerUrl(requireContext())) {
                    UiUtils.navigateToMain(requireActivity(), R.id.user_fragment);
                }
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mPrefListener != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefListener);
        }
    }

}
