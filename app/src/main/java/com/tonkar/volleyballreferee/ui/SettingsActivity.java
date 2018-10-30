package com.tonkar.volleyballreferee.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.Tags;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.i(Tags.SETTINGS, "Create settings activity");

        setTitle(getResources().getString(R.string.settings));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
