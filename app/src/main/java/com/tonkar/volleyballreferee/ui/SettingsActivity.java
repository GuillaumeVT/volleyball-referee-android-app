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

    private SharedPreferences.OnSharedPreferenceChangeListener mStreamOnlineListener;

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

        mStreamOnlineListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equals(PrefUtils.PREF_STREAM_ONLINE)) {
                    ServicesProvider.getInstance().restoreRecordedGamesService(SettingsActivity.this);
                    ServicesProvider.getInstance().getRecordedGamesService().assessAreRecordedOnline();
                }
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(mStreamOnlineListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mStreamOnlineListener != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(mStreamOnlineListener);
        }
    }
}
