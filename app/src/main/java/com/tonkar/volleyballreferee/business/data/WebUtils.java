package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;

import java.util.Locale;

public class WebUtils {

    public static String BASE_URL        = "http://192.168.56.101:8080";
    //public static String BASE_URL        = "https://www.volleyball-referee.com";
    public static String SEARCH_URL      = BASE_URL + "/search";
    public static String LIVE_URL        = BASE_URL + "/search/live";
    public static String VIEW_URL        = BASE_URL + "/view/%d";
    public static String USER_URL        = BASE_URL + "/user/%d";
    public static String GAME_API_URL    = BASE_URL + "/api/manage/game/%d";
    public static String GAME_CODE_URL   = BASE_URL + "/api/view/game/code/%d";
    public static String SET_API_URL     = GAME_API_URL + "/set/%d";
    public static String HAS_MESSAGE_URL = BASE_URL + "/api/message/has";
    public static String MESSAGE_URL     = BASE_URL + "/api/message";


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

    public static void getGameFromCode(Context context, final int code, final AsyncGameRequestListener listener) {
        String url = String.format(Locale.getDefault(), WebUtils.GAME_CODE_URL, code);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, url, new byte[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            Log.e("VBR-WebUtils", "The response was null when getting a game from code");
                            listener.onTechnicalError();
                        } else {
                            RecordedGame recordedGame = JsonIOUtils.readRecordedGame(response);

                            if (recordedGame == null) {
                                Log.e("VBR-WebUtils", "Failed to deserialize a game from code or to notify the listener");
                                listener.onTechnicalError();
                            } else {
                                if (listener != null) {
                                    listener.onRecordedGameReceivedFromCode(recordedGame);
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VBR-WebUtils", "Error while getting a game from code");
                        listener.onInvalidCode();
                    }
                }
        );
        getInstance().getRequestQueue(context).add(stringRequest);
    }

}
