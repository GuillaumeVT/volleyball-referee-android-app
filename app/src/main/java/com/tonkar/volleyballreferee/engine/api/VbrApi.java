package com.tonkar.volleyballreferee.engine.api;

import android.content.Context;
import android.net.*;

import com.tonkar.volleyballreferee.BuildConfig;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.service.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class VbrApi {

    private static final String    BASE_URL             = BuildConfig.SERVER_ADDRESS + "/api/v3.2";
    private static final String    AUTHORIZATION_HEADER = "Authorization";
    private static final MediaType JSON_MEDIA_TYPE      = MediaType.parse("application/json; charset=utf-8");

    private static VbrApi                  sVbrApi;
    private        OkHttpClient            mHttpClient;
    private        TokenExpiredInterceptor mTokenExpiredInterceptor;

    private VbrApi() {}

    public static VbrApi getInstance() {
        if (sVbrApi == null) {
            sVbrApi = new VbrApi();
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

    private Request buildGet(String path, ApiUserToken userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .build();
    }

    public Request buildGet(String path, int page, int size, ApiUserToken userToken) {
        return new Request.Builder()
                .url(HttpUrl
                             .parse(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                             .newBuilder()
                             .addQueryParameter("page", Integer.toString(page))
                             .addQueryParameter("size", Integer.toString(size))
                             .build())
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .build();
    }

    private Request buildGet(String path) {
        return new Request.Builder().url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path)).build();
    }

    private Request buildPost(String path, String jsonBody, ApiUserToken userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPost(String path, ApiUserToken userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .post(RequestBody.create("", JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPost(String path, String jsonBody) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPost(String path) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .post(RequestBody.create("", JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPut(String path, String jsonBody, ApiUserToken userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .put(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPatch(String path, String jsonBody, ApiUserToken userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .patch(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildPatch(String path, ApiUserToken userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .patch(RequestBody.create("", JSON_MEDIA_TYPE))
                .build();
    }

    private Request buildDelete(String path, ApiUserToken userToken) {
        return new Request.Builder()
                .url(String.format(Locale.US, "%s/%s", VbrApi.BASE_URL, path))
                .addHeader(AUTHORIZATION_HEADER, bearerToken(userToken.getToken()))
                .delete()
                .build();
    }

    private String bearerToken(String token) {
        return String.format("Bearer %s", token);
    }

    public void getUser(String purchaseToken, Context context, Callback callback) {
        Request request = buildGet(String.format(Locale.US, "public/users/%s", purchaseToken));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void createUser(ApiNewUser newUser, Context context, Callback callback) {
        final String json = JsonConverters.GSON.toJson(newUser, ApiNewUser.class);
        Request request = buildPost("public/users", json);
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void signInUser(ApiEmailCredentials emailCredentials, Context context, Callback callback) {
        String json = JsonConverters.GSON.toJson(emailCredentials, ApiEmailCredentials.class);
        Request request = buildPost("public/users/token", json);
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void initiateUserPasswordRecovery(String email, Context context, Callback callback) {
        Request request = buildPost(String.format(Locale.US, "public/users/password/recover/%s", email));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void updateUserPassword(ApiUserPasswordUpdate passwordUpdate, Context context, Callback callback) {
        String passwordUpdateStr = JsonConverters.GSON.toJson(passwordUpdate, ApiUserPasswordUpdate.class);
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

    public void acceptFriendRequest(ApiFriendRequest friendRequest, Context context, Callback callback) {
        Request request = buildPost(String.format(Locale.US, "users/friends/accept/%s", friendRequest.getId()),
                                    PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void rejectFriendRequest(ApiFriendRequest friendRequest, Context context, Callback callback) {
        Request request = buildPost(String.format(Locale.US, "users/friends/reject/%s", friendRequest.getId()),
                                    PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void removeFriend(ApiFriend friend, Context context, Callback callback) {
        Request request = buildDelete(String.format(Locale.US, "users/friends/remove/%s", friend.getId()), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void countFriendRequests(Context context, Callback callback) {
        Request request = buildGet("users/friends/received/count", PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void refreshSubscriptionPurchaseToken(Context context, Callback callback) {
        Request request = buildPost(String.format(Locale.US, "users/%s", PrefUtils.getWebPremiumBillingToken(context)),
                                    PrefUtils.getUserToken(context));
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

    public void createLeague(ApiLeague league, Context context, Callback callback) {
        String json = JsonConverters.GSON.toJson(league, ApiLeague.class);
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

    public void upsertRules(ApiRules rules, boolean create, Context context, Callback callback) {
        ApiUserToken userToken = PrefUtils.getUserToken(context);
        String json = JsonConverters.GSON.toJson(rules, ApiRules.class);
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

    public void upsertTeam(ApiTeam team, boolean create, Context context, Callback callback) {
        ApiUserToken userToken = PrefUtils.getUserToken(context);
        String json = JsonConverters.GSON.toJson(team, ApiTeam.class);
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

    public void scheduleGame(ApiGameSummary game, boolean create, Context context, Callback callback) {
        ApiUserToken userToken = PrefUtils.getUserToken(context);
        String json = JsonConverters.GSON.toJson(game, ApiGameSummary.class);
        Request request = create ? buildPost("games", json, userToken) : buildPut("games", json, userToken);
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void createGame(ApiGame game, Context context, Callback callback) {
        String json = JsonConverters.GSON.toJson(game, ApiGame.class);
        Request request = buildPost("games/full", json, PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void updateGame(ApiGame game, Context context, Callback callback) {
        String json = JsonConverters.GSON.toJson(game, ApiGame.class);
        Request request = buildPut("games/full", json, PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void deleteGame(String id, Context context, Callback callback) {
        Request request = buildDelete(String.format(Locale.US, "games/%s", id), PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void updateSet(StoredGame game, Context context, Callback callback) {
        int setIndex = game.currentSetIndex();
        String json = JsonConverters.GSON.toJson(game.getSets().get(setIndex), ApiSet.class);
        Request request = buildPatch(String.format(Locale.US, "games/%s/set/%d", game.getId(), 1 + setIndex), json,
                                     PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }

    public void indexGame(IStoredGame game, Context context, Callback callback) {
        Request request = buildPatch(String.format(Locale.US, "games/%s/indexed/%b", game.getId(), game.isIndexed()),
                                     PrefUtils.getUserToken(context));
        getHttpClient(context).newCall(request).enqueue(callback);
    }
}
