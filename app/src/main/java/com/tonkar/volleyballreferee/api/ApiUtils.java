package com.tonkar.volleyballreferee.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.tonkar.volleyballreferee.BuildConfig;

public class ApiUtils {

    public static String BASE_URL = BuildConfig.SERVER_ADDRESS + "/api/v3";

    public static String WEB_APP_HOME_URL = BASE_URL + "/home";
    public static String WEB_APP_LIVE_URL = BASE_URL + "/search/live";

    public static String MESSAGES_API_URL = BASE_URL + "/public/messages";

    public static String LEAGUES_API_URL = BASE_URL + "/leagues";

    public static String RULES_API_URL = BASE_URL + "/rules";
    public static String RULE_API_URL  = BASE_URL + "/rules/%s";

    public static String TEAMS_API_URL = BASE_URL + "/teams";
    public static String TEAM_API_URL  = BASE_URL + "/teams/%s";

    public static String GAMES_API_URL           = BASE_URL + "/games";
    public static String GAME_API_URL            = BASE_URL + "/games/%s";
    public static String GAME_SET_API_URL        = BASE_URL + "/games/%s/set/%d";
    public static String GAME_INDEXED_API_URL    = BASE_URL + "/games/%s/indexed/%b";
    public static String FULL_GAME_API_URL       = BASE_URL + "/games/full";
    public static String AVAILABLE_GAMES_API_URL = BASE_URL + "/games/available";
    public static String COMPLETED_GAMES_API_URL = BASE_URL + "/games/completed";

    public static String USERS_API_URL            = BASE_URL + "/users";
    public static String USER_API_URL             = BASE_URL + "/users/%s";
    public static String FRIENDS_RECEIVED_API_URL = BASE_URL + "/users/friends/received";
    public static String FRIENDS_REQUEST_API_URL  = BASE_URL + "/users/friends/request/%s";
    public static String FRIENDS_ACCEPT_API_URL   = BASE_URL + "/users/friends/accept/%s";
    public static String FRIENDS_REJECT_API_URL   = BASE_URL + "/users/friends/reject/%s";
    public static String FRIENDS_REMOVE_API_URL   = BASE_URL + "/users/friends/remove/%s";

    private static ApiUtils     sApiUtils;
    private        RequestQueue mRequestQueue;

    private ApiUtils() {
    }

    public static ApiUtils getInstance() {
        if (sApiUtils == null) {
            sApiUtils = new ApiUtils();
        }
        return sApiUtils;
    }

    public RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return mRequestQueue;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
