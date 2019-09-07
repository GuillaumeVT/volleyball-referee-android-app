package com.tonkar.volleyballreferee;

import android.app.Application;

import com.tonkar.volleyballreferee.engine.PrefUtils;

public class VolleyballRefereeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PrefUtils.applyNightMode(getApplicationContext());
    }

}
