package com.tonkar.volleyballreferee.engine.api;

import android.content.*;
import android.net.*;

import androidx.preference.PreferenceManager;

import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.service.StoredGame;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class VbrApi {

    private static final String    AUTHORIZATION_HEADER = "Authorization";
    private static final MediaType JSON_MEDIA_TYPE      = MediaType.parse("application/json; charset=utf-8");

    private static VbrApi                  sVbrApi;
    private final  String                  mBaseUrl;
    private        OkHttpClient            mHttpClient;
    private        TokenExpiredInterceptor mTokenExpiredInterceptor;

    private VbrApi(String serverUrl) {
        mBaseUrl = serverUrl + "/api";
    }

    public static VbrApi getInstance(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = sharedPreferences.getString(PrefUtils.PREF_SERVER_URL, null);

        if (sVbrApi == null || !sVbrApi.mBaseUrl.equals(serverUrl)) {
            sVbrApi = new VbrApi(serverUrl);
        }

        return sVbrApi;
    }

    private OkHttpClient getHttpClient(Context context) {
        if (mHttpClient == null) {
            mTokenExpiredInterceptor = new TokenExpiredInterceptor(context.getApplicationContext());
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(40, TimeUnit.SECONDS)
                    .addInterceptor(mTokenExpiredInterceptor)
                    .build();
        } else {
            mTokenExpiredInterceptor.setContext(context.getApplicationContext());
        }

        return mHttpClient;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private Request buildGet(String path, UserTokenDto userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .build();
    }

    public Request buildGet(String path, int page, int size, UserTokenDto userToken) {
        return new Request.Builder()
                .url(HttpUrl
                             .parse(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                             .newBuilder()
                             .addQueryParameter("page", Integer.toString(page))
                             .addQueryParameter("size", Integer.toString(size))
                             .build())
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .build();
    }

    private Request buildPost(String path, String jsonBody, UserTokenDto userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPost(String path, UserTokenDto userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .post(RequestBody.create("", JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPost(String path, String jsonBody) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPut(String path, String jsonBody, UserTokenDto userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .put(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPatch(String path, String jsonBody, UserTokenDto userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .patch(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildDelete(String path, UserTokenDto userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", mBaseUrl, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .delete()
                .build();
    }

    private String bearerToken(String token) {
        return String.format("Bearer %s", token);
    }

    public void signInUser(LoginCredentialsDto loginCredentials, Context context, Callback callback) {
        String json = JsonConverters.GSON.toJson(loginCredentials, LoginCredentialsDto.class);
        Request request = buildPost("public/users/token", json);
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void updateUserPassword(UserPasswordUpdateDto passwordUpdate, Context context, Callback callback) {
        String passwordUpdateStr = JsonConverters.GSON.toJson(passwordUpdate, UserPasswordUpdateDto.class);
        Request request = buildPatch("users/password", passwordUpdateStr, PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getFriendsAndRequests(Context context, Callback callback) {
        Request request = buildGet("users/friends", PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void sendFriendRequest(String friendPseudo, Context context, Callback callback) {
        Request request = buildPost(String.format(Locale.US, "users/friends/request/%s", friendPseudo), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void acceptFriendRequest(FriendRequestDto friendRequest, Context context, Callback callback) {
        Request request = buildPost(String.format(Locale.US, "users/friends/accept/%s", friendRequest.getId()),
                                    PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void rejectFriendRequest(FriendRequestDto friendRequest, Context context, Callback callback) {
        Request request = buildPost(String.format(Locale.US, "users/friends/reject/%s", friendRequest.getId()),
                                    PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void removeFriend(FriendDto friend, Context context, Callback callback) {
        Request request = buildDelete(String.format(Locale.US, "users/friends/remove/%s", friend.getId()), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void countFriendRequests(Context context, Callback callback) {
        Request request = buildGet("users/friends/received/count", PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getLeagueList(Context context, Callback callback) {
        Request request = buildGet("leagues", PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getLeague(String id, Context context, Callback callback) {
        Request request = buildGet(String.format(Locale.US, "leagues/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void createLeague(LeagueDto league, Context context, Callback callback) {
        String json = JsonConverters.GSON.toJson(league, LeagueDto.class);
        Request request = buildPost("leagues", json, PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getRulesList(Context context, Callback callback) {
        Request request = buildGet("rules", PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getRules(String id, Context context, Callback callback) {
        Request request = buildGet(String.format(Locale.US, "rules/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void deleteRules(String id, Context context, Callback callback) {
        Request request = buildDelete(String.format(Locale.US, "rules/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void upsertRules(RulesDto rules, boolean create, Context context, Callback callback) {
        UserTokenDto userToken = PrefUtils.getUserToken(context);
        String json = JsonConverters.GSON.toJson(rules, RulesDto.class);
        Request request = create ? buildPost("rules", json, userToken) : buildPut("rules", json, userToken);
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getTeamPage(int page, int size, Context context, Callback callback) {
        Request request = buildGet("teams", page, size, PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getTeam(String id, Context context, Callback callback) {
        Request request = buildGet(String.format(Locale.US, "teams/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void deleteTeam(String id, Context context, Callback callback) {
        Request request = buildDelete(String.format(Locale.US, "teams/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void upsertTeam(TeamDto team, boolean create, Context context, Callback callback) {
        UserTokenDto userToken = PrefUtils.getUserToken(context);
        String json = JsonConverters.GSON.toJson(team, TeamDto.class);
        Request request = create ? buildPost("teams", json, userToken) : buildPut("teams", json, userToken);
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void countAvailableGames(Context context, Callback callback) {
        Request request = buildGet("games/available/count", PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getAvailableGames(Context context, Callback callback) {
        Request request = buildGet("games/available", PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getCompletedGames(int page, int size, Context context, Callback callback) {
        Request request = buildGet("games/completed", page, size, PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void getGame(String id, Context context, Callback callback) {
        Request request = buildGet(String.format(Locale.US, "games/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void scheduleGame(GameSummaryDto game, boolean create, Context context, Callback callback) {
        UserTokenDto userToken = PrefUtils.getUserToken(context);
        String json = JsonConverters.GSON.toJson(game, GameSummaryDto.class);
        Request request = create ? buildPost("games", json, userToken) : buildPut("games", json, userToken);
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void upsertGame(GameDto game, Context context, Callback callback) {
        String json = JsonConverters.GSON.toJson(game, GameDto.class);
        Request request = buildPost("games/full", json, PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void deleteGame(String id, Context context, Callback callback) {
        Request request = buildDelete(String.format(Locale.US, "games/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void updateSet(StoredGame game, Context context, Callback callback) {
        int setIndex = game.currentSetIndex();
        String json = JsonConverters.GSON.toJson(game.getSets().get(setIndex), SetDto.class);
        Request request = buildPatch(String.format(Locale.US, "games/%s/set/%d", game.getId(), 1 + setIndex), json,
                                     PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }
}
