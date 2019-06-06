package com.tonkar.volleyballreferee.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tonkar.volleyballreferee.BuildConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.concurrent.TimeUnit;

public class ApiUtils {

    public static String WEB_APP_VIEW_GAME  = BuildConfig.SERVER_ADDRESS + "/view/game/%s";
    public static String WEB_APP_LIVE_GAMES = BuildConfig.SERVER_ADDRESS + "/search/live";

    public static String BASE_URL = BuildConfig.SERVER_ADDRESS + "/api/v3";

    public static String MESSAGES_API_URL = BASE_URL + "/public/messages";

    public static String LEAGUES_API_URL = BASE_URL + "/leagues";
    public static String LEAGUE_API_URL  = BASE_URL + "/leagues/%s";

    public static String RULES_API_URL = BASE_URL + "/rules";
    public static String RULE_API_URL  = BASE_URL + "/rules/%s";

    public static String TEAMS_API_URL = BASE_URL + "/teams";
    public static String TEAM_API_URL  = BASE_URL + "/teams/%s";

    public static String GAMES_API_URL                 = BASE_URL + "/games";
    public static String GAME_API_URL                  = BASE_URL + "/games/%s";
    public static String GAME_SET_API_URL              = BASE_URL + "/games/%s/set/%d";
    public static String GAME_INDEXED_API_URL          = BASE_URL + "/games/%s/indexed/%b";
    public static String FULL_GAME_API_URL             = BASE_URL + "/games/full";
    public static String AVAILABLE_GAMES_API_URL       = BASE_URL + "/games/available";
    public static String COMPLETED_GAMES_API_URL       = BASE_URL + "/games/completed";
    public static String AVAILABLE_GAMES_COUNT_API_URL = AVAILABLE_GAMES_API_URL + "/count";

    public static String USERS_API_URL                  = BASE_URL + "/users";
    public static String USER_API_URL                   = BASE_URL + "/public/users/" + BuildConfig.SIGN_UP_KEY;
    public static String FRIENDS_API_URL                = BASE_URL + "/users/friends";
    public static String FRIENDS_REQUEST_API_URL        = BASE_URL + "/users/friends/request/%s";
    public static String FRIENDS_ACCEPT_API_URL         = BASE_URL + "/users/friends/accept/%s";
    public static String FRIENDS_REJECT_API_URL         = BASE_URL + "/users/friends/reject/%s";
    public static String FRIENDS_REMOVE_API_URL         = BASE_URL + "/users/friends/remove/%s";
    public static String FRIENDS_RECEIVED_COUNT_API_URL = BASE_URL + "/users/friends/received/count";

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static ApiUtils     sApiUtils;
    private        OkHttpClient mHttpClient;

    private ApiUtils() {}

    public static ApiUtils getInstance() {
        if (sApiUtils == null) {
            sApiUtils = new ApiUtils();
        }
        return sApiUtils;
    }

    public OkHttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }

        return mHttpClient;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static Request buildGet(String url, Authentication authentication) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", authentication.getToken()))
                .addHeader("AuthenticationProvider", authentication.getProvider().toString())
                .build();
    }

    public static Request buildGet(String url) {
        return new Request.Builder()
                .url(url)
                .build();
    }

    public static Request buildPost(String url, String jsonBody, Authentication authentication) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", authentication.getToken()))
                .addHeader("AuthenticationProvider", authentication.getProvider().toString())
                .post(RequestBody.create(ApiUtils.JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPost(String url, Authentication authentication) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", authentication.getToken()))
                .addHeader("AuthenticationProvider", authentication.getProvider().toString())
                .post(RequestBody.create(ApiUtils.JSON_MEDIA_TYPE, ""))
                .build();
    }

    public static Request buildPost(String url, String jsonBody) {
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(ApiUtils.JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPut(String url, String jsonBody, Authentication authentication) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", authentication.getToken()))
                .addHeader("AuthenticationProvider", authentication.getProvider().toString())
                .put(RequestBody.create(ApiUtils.JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPatch(String url, String jsonBody, Authentication authentication) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", authentication.getToken()))
                .addHeader("AuthenticationProvider", authentication.getProvider().toString())
                .patch(RequestBody.create(ApiUtils.JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPatch(String url, Authentication authentication) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", authentication.getToken()))
                .addHeader("AuthenticationProvider", authentication.getProvider().toString())
                .patch(RequestBody.create(ApiUtils.JSON_MEDIA_TYPE, ""))
                .build();
    }

    public static Request buildDelete(String url, Authentication authentication) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", authentication.getToken()))
                .addHeader("AuthenticationProvider", authentication.getProvider().toString())
                .delete()
                .build();
    }
}
