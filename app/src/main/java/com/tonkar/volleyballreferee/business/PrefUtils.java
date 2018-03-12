package com.tonkar.volleyballreferee.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

public class PrefUtils {

    public static final String PREF_STREAM_ONLINE = "pref_stream_online";
    public static final String PREF_REFEREE_NAME  = "pref_referee_name";

    public static boolean isPrefOnlineRecordingEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_STREAM_ONLINE, true);
    }

    public static String getPrefRefereeName(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return getPrefRefereeName(sharedPreferences);
    }

    public static String getPrefRefereeName(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(PREF_REFEREE_NAME, "");
    }

}
