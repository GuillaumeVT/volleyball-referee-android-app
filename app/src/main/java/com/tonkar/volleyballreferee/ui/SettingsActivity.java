package com.tonkar.volleyballreferee.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.IdRes;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;

public class SettingsActivity extends NavigationActivity {

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
    }

}
