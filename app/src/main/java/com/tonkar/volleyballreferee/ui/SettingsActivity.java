package com.tonkar.volleyballreferee.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.i("VBR-SettingsActivity", "Create settings activity");

        setTitle(getResources().getString(R.string.settings));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                switch (key) {
                    case PrefUtils.PREF_SYNC_DATA:
                        ServicesProvider.getInstance().restoreSavedTeamsService(getApplicationContext());
                        ServicesProvider.getInstance().getSavedTeamsService().loadSavedTeams();
                        ServicesProvider.getInstance().restoreSavedRulesService(getApplicationContext());
                        ServicesProvider.getInstance().getSavedRulesService().loadSavedRules();
                        ServicesProvider.getInstance().restoreRecordedGamesService(getApplicationContext());
                        ServicesProvider.getInstance().getRecordedGamesService().loadRecordedGames();
                        break;
                    default:
                        break;
                }
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
