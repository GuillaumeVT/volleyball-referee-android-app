package com.tonkar.volleyballreferee.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.tonkar.volleyballreferee.business.data.WebUtils;
import com.tonkar.volleyballreferee.interfaces.data.UserId;

public class PrefUtils {

    public static final  String PREF_SYNC_DATA             = "pref_sync_data";
    public static final  String PREF_REFEREE_NAME          = "pref_referee_name";
    private static final String PREF_USER_SIGNED_IN        = "pref_user_connected";
    private static final String PREF_USER_ID               = "pref_user_identifier";
    private static final String PREF_PURCHASED_WEB_PREMUIM = "pref_purchased_web_premium";

    public static boolean isPrefDataSyncEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_SYNC_DATA, true);
    }

    public static String getPrefRefereeName(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return getPrefRefereeName(sharedPreferences);
    }

    public static String getPrefRefereeName(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(PREF_REFEREE_NAME, "");
    }

    public static void signIn(Context context, String userId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_USER_ID, userId).putBoolean(PREF_USER_SIGNED_IN, true).apply();
    }

    public static void signOut(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove(PREF_USER_ID).putBoolean(PREF_USER_SIGNED_IN, false).apply();
    }

    public static boolean isSignedIn(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_USER_SIGNED_IN, false);
    }

    public static String getUserId(Context context) {
        final String userId;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(PREF_USER_SIGNED_IN, false)) {
            userId = sharedPreferences.getString(PREF_USER_ID, UserId.VBR_USER_ID);
        } else {
            userId = UserId.VBR_USER_ID;
        }

        return userId;
    }

    public static void purchaseWebPremium(Context context, boolean purchased) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(PREF_PURCHASED_WEB_PREMUIM, purchased).apply();
    }

    public static boolean isWebPremiumPurchased(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_PURCHASED_WEB_PREMUIM, false);
    }

    public static boolean canRequest(Context context) {
        return isPrefDataSyncEnabled(context) && WebUtils.isConnectedToInternet(context);
    }

    public static boolean canSync(Context context) {
        return isWebPremiumPurchased(context) && isSignedIn(context);
    }

    public static boolean isSyncOn(Context context) {
        return canRequest(context) && canSync(context);
    }
}
