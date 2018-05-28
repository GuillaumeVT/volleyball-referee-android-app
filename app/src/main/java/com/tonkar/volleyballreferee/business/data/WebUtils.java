package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WebUtils {

    public static String BASE_URL                 = "https://www.volleyball-referee.com";
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

    public void getGameFromCode(Context context, final int code, final AsyncGameRequestListener listener) {
        String url = String.format(Locale.getDefault(), WebUtils.GAME_CODE_URL, code);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, url, new byte[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        RecordedGame recordedGame = JsonIOUtils.readRecordedGame(response);

                        if (recordedGame == null) {
                            Log.e("VBR-WebUtils", "Failed to deserialize a game from code or to notify the listener");
                            listener.onInternalError();
                        } else {
                            if (listener != null) {
                                listener.onRecordedGameReceivedFromCode(recordedGame);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            Log.e("VBR-WebUtils", String.format(Locale.getDefault(), "Error %d getting a game from code", error.networkResponse.statusCode));
                            if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                listener.onNotFound();
                            } else {
                                listener.onError();
                            }
                        } else {
                            listener.onError();
                        }
                    }
                }
        );
        getRequestQueue(context).add(stringRequest);
    }

    public void getUserGame(Context context, final String userId, final long id, final AsyncGameRequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("id", String.valueOf(id));
        String parameters = JsonStringRequest.getParameters(params);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_GAME_URL + parameters, new byte[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        RecordedGame recordedGame = JsonIOUtils.readRecordedGame(response);

                        if (recordedGame == null) {
                            Log.e("VBR-WebUtils", "Failed to deserialize a user game or to notify the listener");
                            listener.onInternalError();
                        } else {
                            if (listener != null) {
                                listener.onUserGameReceived(recordedGame);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            Log.e("VBR-WebUtils", String.format(Locale.getDefault(), "Error %d getting a user game", error.networkResponse.statusCode));
                            if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                listener.onNotFound();
                            } else {
                                listener.onError();
                            }
                        } else {
                            listener.onError();
                        }
                    }
                }
        );
        getRequestQueue(context).add(stringRequest);
    }

    public void getUserScheduledGames(Context context, final String userId, final AsyncGameRequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        String parameters = JsonStringRequest.getParameters(params);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_SCHEDULED_GAMES_URL + parameters, new byte[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<GameDescription> gameDescriptionList = JsonIOUtils.readGameDescriptionList(response);

                        if (gameDescriptionList == null) {
                            Log.e("VBR-WebUtils", "Failed to deserialize a user scheduled game list or to notify the listener");
                            listener.onInternalError();
                        } else {
                            if (listener != null) {
                                listener.onUserGameListReceived(gameDescriptionList);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            Log.e("VBR-WebUtils", String.format(Locale.getDefault(), "Error %d getting a user scheduled game list", error.networkResponse.statusCode));
                        }
                        listener.onError();
                    }
                }
        );
        getRequestQueue(context).add(stringRequest);
    }

}
