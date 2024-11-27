package com.tonkar.volleyballreferee.engine;

import android.content.*;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.tonkar.volleyballreferee.engine.api.*;
import com.tonkar.volleyballreferee.engine.api.model.*;

import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

public class PrefUtils {

    public static final String PREF_KEEP_SCREEN_ON = "pref_keep_screen_on";
    public static final String PREF_NIGHT_MODE     = "pref_night_mode";
    public static final String PREF_SERVER_URL     = "pref_server_url";

    private static final String PREF_USER              = "pref_user_4.3";
    private static final String PREF_USER_TOKEN        = "pref_user_token_4.3";
    private static final String PREF_USER_TOKEN_EXPIRY = "pref_user_token_expiry_4.3";

    public static final String PREF_ONBOARDING_MAIN = "pref_onboarding_main";

    public static boolean hasServerUrl(Context context) {
        String url = getServerUrl(context);

        if (StringUtils.isNotBlank(url)) {
            try {
                new URL(url).toURI();
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String getServerUrl(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PrefUtils.PREF_SERVER_URL, null);
    }

    public static void signIn(Context context, UserTokenDto userToken) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(PREF_USER, JsonConverters.GSON.toJson(userToken.getUser()))
                .putString(PREF_USER_TOKEN, userToken.getToken())
                .putLong(PREF_USER_TOKEN_EXPIRY, userToken.getTokenExpiry())
                .apply();
    }

    public static UserSummaryDto getUser(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userStr = sharedPreferences.getString(PREF_USER, null);
        if (StringUtils.isNotBlank(userStr)) {
            return JsonConverters.GSON.fromJson(userStr, UserSummaryDto.class);
        } else {
            return null;
        }
    }

    public static String getUserId(Context context) {
        return Optional.ofNullable(getUser(context)).map(UserSummaryDto::getId).orElse(null);
    }

    public static void signOut(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove(PREF_USER).remove(PREF_USER_TOKEN).remove(PREF_USER_TOKEN_EXPIRY).apply();
    }

    public static boolean isSignedIn(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return StringUtils.isNotBlank(sharedPreferences.getString(PREF_USER, null)) && StringUtils.isNotBlank(
                sharedPreferences.getString(PREF_USER_TOKEN, null)) && Calendar
                .getInstance(TimeZone.getTimeZone("UTC"))
                .getTime()
                .getTime() < sharedPreferences.getLong(PREF_USER_TOKEN_EXPIRY, 0L);
    }

    public static UserTokenDto getUserToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userStr = sharedPreferences.getString(PREF_USER, null);
        String tokenStr = sharedPreferences.getString(PREF_USER_TOKEN, null);
        long tokenExpiry = sharedPreferences.getLong(PREF_USER_TOKEN_EXPIRY, 0L);

        final UserTokenDto userToken;

        if (StringUtils.isNotBlank(userStr) && StringUtils.isNotBlank(tokenStr)) {
            userToken = new UserTokenDto(JsonConverters.GSON.fromJson(userStr, UserSummaryDto.class), tokenStr, tokenExpiry);
        } else {
            userToken = null;
        }

        return userToken;
    }

    public static boolean canSync(Context context) {
        return hasServerUrl(context) && VbrApi.isConnectedToInternet(context) && isSignedIn(context);
    }

    public static void applyNightMode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String nightMode = sharedPreferences.getString(PrefUtils.PREF_NIGHT_MODE, "system");

        switch (nightMode) {
            case "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            case "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            case "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            default -> {
            }
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

    public static boolean showOnboarding(Context context, String pref) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !sharedPreferences.getBoolean(pref, false);
    }

    public static void completeOnboarding(Context context, String pref) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(pref, true).apply();
    }
}
