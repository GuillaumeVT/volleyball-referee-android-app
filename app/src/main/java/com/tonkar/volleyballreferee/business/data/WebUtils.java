package com.tonkar.volleyballreferee.business.data;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class WebUtils {

    public static String BASE_URL            = "https://www.volleyball-referee.com";
    public static String SEARCH_URL          = BASE_URL + "/search";
    public static String LIVE_URL            = BASE_URL + "/search/live";
    public static String VIEW_INDOOR_URL     = BASE_URL + "/indoor/%d";
    public static String VIEW_BEACH_URL      = BASE_URL + "/beach/%d";
    public static String VIEW_INDOOR_4X4_URL = BASE_URL + "/indoor-4x4/%d";
    public static String VIEW_TIME_BASED_URL = BASE_URL + "/time-based/%d";
    public static String GAME_API_URL        = BASE_URL + "/api/manage/game/%d";
    public static String SET_API_URL         = GAME_API_URL + "/set/%d";
    public static String HAS_MESSAGE_URL     = BASE_URL + "/api/message/has";
    public static String MESSAGE_URL         = BASE_URL + "/api/message";


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
