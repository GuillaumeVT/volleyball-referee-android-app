package com.tonkar.volleyballreferee.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.tonkar.volleyballreferee.interfaces.data.UserId;

public class PrefUtils {

    public static final  String PREF_STREAM_ONLINE  = "pref_stream_online";
    public static final  String PREF_REFEREE_NAME   = "pref_referee_name";
    private static final String PREF_USER_SIGNED_IN = "pref_user_signed_in";
    private static final String PREF_USER_SOCIAL_ID = "pref_user_social_id";
    private static final String PREF_USER_PROVIDER  = "pref_user_provider";

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

    public static void signIn(Context context, String socialId, String provider) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_USER_SOCIAL_ID, socialId).putString(PREF_USER_PROVIDER, provider).putBoolean(PREF_USER_SIGNED_IN, true).apply();
    }

    public static void signOut(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove(PREF_USER_SOCIAL_ID).remove(PREF_USER_PROVIDER).putBoolean(PREF_USER_SIGNED_IN, false).apply();
    }

    public static boolean isSignedIn(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_USER_SIGNED_IN, false);
    }

    public static UserId getUserId(Context context) {
        final UserId userId;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(PREF_USER_SIGNED_IN, false)) {
            userId = new UserId(sharedPreferences.getString(PREF_USER_SOCIAL_ID, UserId.VBR_USER_ID.getSocialId()), sharedPreferences.getString(PREF_USER_PROVIDER, UserId.VBR_USER_ID.getProvider()));
        } else {
            userId = UserId.VBR_USER_ID;
        }

        return userId;
    }
}
