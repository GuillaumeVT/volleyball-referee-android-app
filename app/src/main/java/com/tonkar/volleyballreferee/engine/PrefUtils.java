package com.tonkar.volleyballreferee.engine;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.tonkar.volleyballreferee.engine.stored.JsonIOUtils;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserToken;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUtils;

import java.util.Calendar;
import java.util.TimeZone;

public class PrefUtils {

    public static final String PREF_KEEP_SCREEN_ON            = "pref_keep_screen_on";
    public static final String PREF_NIGHT_MODE                = "pref_night_mode";
    public static final String PREF_INTERACTIVE_NOTIFICATIONS = "pref_interactive_notification";

    private static final String PREF_USER                                   = "pref_user";
    private static final String PREF_USER_TOKEN                             = "pref_user_token";
    private static final String PREF_USER_TOKEN_EXPIRY                      = "pref_user_token_expiry";
    private static final String PREF_WEB_PREMIUM_BILLING_TOKEN              = "pref_web_premium_token";
    private static final String PREF_WEB_PREMIUM_SUBSCRIPTION_BILLING_TOKEN = "pref_web_premium_subscription_token";
    private static final String PREF_SCORE_SHEETS_BILLING_TOKEN             = "pref_score_sheets_token";

    public static void signIn(Context context, ApiUserToken userToken) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(PREF_USER, JsonIOUtils.GSON.toJson(userToken.getUser()))
                .putString(PREF_USER_TOKEN, userToken.getToken())
                .putLong(PREF_USER_TOKEN_EXPIRY, userToken.getTokenExpiry())
                .apply();
    }

    public static void storeUser(Context context, ApiUserSummary user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(PREF_USER, JsonIOUtils.GSON.toJson(user))
                .apply();
    }

    public static ApiUserSummary getUser(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userStr = sharedPreferences.getString(PREF_USER, "");
        if ("".equals(userStr)) {
            return ApiUserSummary.emptyUser();
        } else {
            return JsonIOUtils.GSON.fromJson(userStr, ApiUserSummary.class);
        }
    }

    public static void signOut(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .remove(PREF_USER_TOKEN)
                .apply();
    }

    public static boolean isSignedIn(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !"".equals(sharedPreferences.getString(PREF_USER, ""))
                && !"".equals(sharedPreferences.getString(PREF_USER_TOKEN, ""))
                && Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime() < sharedPreferences.getLong(PREF_USER_TOKEN_EXPIRY, 0L);
    }

    public static ApiUserToken getUserToken(Context context) {
        ApiUserToken userToken;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userStr = sharedPreferences.getString(PREF_USER, "");
        String tokenStr = sharedPreferences.getString(PREF_USER_TOKEN, "");
        long tokenExpiry = sharedPreferences.getLong(PREF_USER_TOKEN_EXPIRY, 0L);

        if (!"".equals(userStr) && !"".equals(tokenStr)) {
            userToken = new ApiUserToken(JsonIOUtils.GSON.fromJson(userStr, ApiUserSummary.class), tokenStr, tokenExpiry);
        } else {
            userToken = new ApiUserToken(ApiUserSummary.emptyUser(), "", 0L);
        }

        return userToken;
    }

    public static void purchaseWebPremium(Context context, String purchaseToken) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_WEB_PREMIUM_BILLING_TOKEN, purchaseToken).apply();
    }

    public static void unpurchaseWebPremium(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove(PREF_WEB_PREMIUM_BILLING_TOKEN).apply();
    }

    public static boolean isWebPremiumPurchased(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !"".equals(sharedPreferences.getString(PREF_WEB_PREMIUM_BILLING_TOKEN, ""));
    }

    public static void subscribeWebPremium(Context context, String subscriptionToken) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_WEB_PREMIUM_SUBSCRIPTION_BILLING_TOKEN, subscriptionToken).apply();
    }

    public static void unsubscribeWebPremium(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove(PREF_WEB_PREMIUM_SUBSCRIPTION_BILLING_TOKEN).apply();
    }

    public static boolean isWebPremiumSubscribed(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !"".equals(sharedPreferences.getString(PREF_WEB_PREMIUM_SUBSCRIPTION_BILLING_TOKEN, ""));
    }

    public static String getWebPremiumBillingToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (isWebPremiumSubscribed(context)) {
            return sharedPreferences.getString(PREF_WEB_PREMIUM_SUBSCRIPTION_BILLING_TOKEN, "");
        } else {
            return sharedPreferences.getString(PREF_WEB_PREMIUM_BILLING_TOKEN, "");
        }
    }

    public static void purchaseScoreSheets(Context context, String purchaseToken) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_SCORE_SHEETS_BILLING_TOKEN, purchaseToken).apply();
    }

    public static void unpurchaseScoreSheets(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove(PREF_SCORE_SHEETS_BILLING_TOKEN).apply();
    }

    public static boolean isScoreSheetsPurchased(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !"".equals(sharedPreferences.getString(PREF_SCORE_SHEETS_BILLING_TOKEN, ""));
    }

    public static boolean canSync(Context context) {
        return (isWebPremiumPurchased(context) || isWebPremiumSubscribed(context)) && ApiUtils.isConnectedToInternet(context) && isSignedIn(context);
    }

    public static boolean shouldSignIn(Context context) {
        return (isWebPremiumPurchased(context) || isWebPremiumSubscribed(context)) && ApiUtils.isConnectedToInternet(context) && !isSignedIn(context);
    }

    public static void applyNightMode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String nightMode = sharedPreferences.getString(PrefUtils.PREF_NIGHT_MODE, "system");

        switch (nightMode) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                break;
        }
    }

    public static String getNightMode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PrefUtils.PREF_NIGHT_MODE, "system");
    }

    public static void setNightMode(Context context, String nightMode) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_NIGHT_MODE, nightMode).apply();
        applyNightMode(context);
    }

}
