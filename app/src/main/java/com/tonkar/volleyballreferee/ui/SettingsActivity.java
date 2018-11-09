package com.tonkar.volleyballreferee.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.IdRes;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.Tags;

import androidx.preference.PreferenceManager;

public class SettingsActivity extends NavigationActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener;

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.settings);
    }

    @Override
    protected @IdRes int getCheckedItem() {
        return R.id.action_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.i(Tags.SETTINGS, "Create settings activity");

        initNavigationMenu();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mPreferencesListener = (prefs, key) -> {
            switch (key) {
                case PrefUtils.PREF_SYNC_DATA:
                    ServicesProvider.getInstance().getSavedRulesService(getApplicationContext()).syncRulesOnline();
                    ServicesProvider.getInstance().getSavedTeamsService(getApplicationContext()).syncTeamsOnline();
                    ServicesProvider.getInstance().getRecordedGamesService(getApplicationContext()).syncGamesOnline();
                    break;
                default:
                    break;
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(mPreferencesListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPreferencesListener != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
        }
    }
}
