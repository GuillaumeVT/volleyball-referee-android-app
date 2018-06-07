package com.tonkar.volleyballreferee.business.data;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.tonkar.volleyballreferee.BuildConfig;

public class WebUtils {

    public static String BASE_URL                 = BuildConfig.SERVER_ADDRESS;
    public static String SEARCH_URL               = BASE_URL + "/search";
    public static String LIVE_URL                 = BASE_URL + "/search/live";
    public static String VIEW_URL                 = BASE_URL + "/game/%d";
    public static String USER_URL                 = BASE_URL + "/user";
    public static String GAME_API_URL             = BASE_URL + "/api/manage/game/%d";
    public static String SET_API_URL              = GAME_API_URL + "/set/%d";
    public static String HAS_MESSAGE_URL          = BASE_URL + "/api/message/has";
    public static String MESSAGE_URL              = BASE_URL + "/api/message";
    public static String GAME_CODE_URL            = BASE_URL + "/api/view/game/code/%d";
    public static String USER_GAME_URL            = BASE_URL + "/api/user/game";
    public static String USER_SCHEDULED_GAMES_URL = USER_GAME_URL + "/available";
    public static String USER_RULES_URL           = BASE_URL + "/api/user/rules";
    public static String USER_TEAM_URL            = BASE_URL + "/api/user/team";

    private static WebUtils     sWebUtils;
    private        RequestQueue mRequestQueue;

    private WebUtils() {
    }

    public static WebUtils getInstance() {
        if (sWebUtils == null) {
            sWebUtils = new WebUtils();
        }
        return sWebUtils;
    }

    public RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return mRequestQueue;
    }

}
