package com.tonkar.volleyballreferee.business;

import android.content.Context;
import android.content.SharedPreferences;

import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.api.ApiUtils;

import androidx.preference.PreferenceManager;

public class PrefUtils {

    public static final  String PREF_KEEP_SCREEN_ON            = "pref_keep_screen_on";
    public static final  String PREF_INTERACTIVE_NOTIFICATIONS = "pref_interactive_notification";
    private static final String PREF_USER_SIGNED_IN            = "pref_user_connected";
    private static final String PREF_USER_ID                   = "pref_user_identifier";
    private static final String PREF_USER_PSEUDO               = "pref_user_pseudo";
    private static final String PREF_USER_TOKEN                = "pref_user_token";
    private static final String PREF_PURCHASED_WEB_PREMUIM     = "pref_purchased_web_premium";

    public static void signIn(Context context, Authentication authentication) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(PREF_USER_ID, authentication.getUserId())
                .putString(PREF_USER_TOKEN, authentication.getToken())
                .putString(PREF_USER_PSEUDO, authentication.getUserPseudo())
                .putBoolean(PREF_USER_SIGNED_IN, true)
                .apply();
    }

    public static void signOut(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .remove(PREF_USER_ID)
                .remove(PREF_USER_TOKEN)
                .remove(PREF_USER_PSEUDO)
                .putBoolean(PREF_USER_SIGNED_IN, false)
                .apply();
    }

    public static void storeUserPseudo(Context context, String pseudo) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(PREF_USER_PSEUDO, pseudo)
                .apply();
    }

    public static boolean isSignedIn(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_USER_SIGNED_IN, false);
    }

    public static Authentication getAuthentication(Context context) {
        Authentication authentication;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean(PREF_USER_SIGNED_IN, false)) {
            String userId = sharedPreferences.getString(PREF_USER_ID, Authentication.VBR_USER_ID);
            String userPseudo = sharedPreferences.getString(PREF_USER_PSEUDO, "");
            String token = sharedPreferences.getString(PREF_USER_TOKEN, "");
            authentication = Authentication.of(userId, userPseudo, token);
        } else {
            authentication = Authentication.of(Authentication.VBR_USER_ID, "", "");
        }

        return authentication;
    }

    public static void purchaseWebPremium(Context context, boolean purchased) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(PREF_PURCHASED_WEB_PREMUIM, purchased).apply();
    }

    private static boolean isWebPremiumPurchased(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_PURCHASED_WEB_PREMUIM, false);
    }

    private static boolean userExists(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !"".equals(sharedPreferences.getString(PREF_USER_PSEUDO, ""));
    }

    public static String getUserPseudo(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PREF_USER_PSEUDO, "");
    }

    public static boolean canSync(Context context) {
        return isWebPremiumPurchased(context) && ApiUtils.isConnectedToInternet(context) && isSignedIn(context) && userExists(context);
    }

    public static boolean shouldSignIn(Context context) {
        return isWebPremiumPurchased(context) && ApiUtils.isConnectedToInternet(context) && !isSignedIn(context);
    }

    public static boolean shouldCreateUser(Context context) {
        return isWebPremiumPurchased(context) && ApiUtils.isConnectedToInternet(context) && isSignedIn(context) && !userExists(context);
    }
}
