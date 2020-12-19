package com.tonkar.volleyballreferee.engine.stored.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tonkar.volleyballreferee.BuildConfig;
import com.tonkar.volleyballreferee.engine.PrefUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiUtils {

    public static final  String    BASE_URL        = BuildConfig.SERVER_ADDRESS + "/api/v3.2";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static ApiUtils                sApiUtils;
    private        OkHttpClient            mHttpClient;
    private        TokenExpiredInterceptor mTokenExpiredInterceptor;

    private ApiUtils() {}

    public static ApiUtils getInstance() {
        if (sApiUtils == null) {
            sApiUtils = new ApiUtils();
        }
        return sApiUtils;
    }

    public OkHttpClient getHttpClient(Context context) {
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
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static Request buildGet(String url, ApiUserToken userToken) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .build();
    }

    public static Request buildGet(String url, int page, int size, ApiUserToken userToken) {
        return new Request.Builder()
                .url(HttpUrl.parse(url).newBuilder()
                        .addQueryParameter("page", Integer.toString(page))
                        .addQueryParameter("size", Integer.toString(size))
                        .build())
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .build();
    }

    public static Request buildGet(String url) {
        return new Request.Builder()
                .url(url)
                .build();
    }

    public static Request buildPost(String url, String jsonBody, ApiUserToken userToken) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .post(RequestBody.create(JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPost(String url, ApiUserToken userToken) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .post(RequestBody.create(JSON_MEDIA_TYPE, ""))
                .build();
    }

    public static Request buildPost(String url, String jsonBody) {
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPost(String url) {
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON_MEDIA_TYPE, ""))
                .build();
    }

    public static Request buildPut(String url, String jsonBody, ApiUserToken userToken) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .put(RequestBody.create(JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPatch(String url, String jsonBody, ApiUserToken userToken) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .patch(RequestBody.create(JSON_MEDIA_TYPE, jsonBody))
                .build();
    }

    public static Request buildPatch(String url, ApiUserToken userToken) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .patch(RequestBody.create(JSON_MEDIA_TYPE, ""))
                .build();
    }

    public static Request buildDelete(String url, ApiUserToken userToken) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", String.format("Bearer %s", userToken.getToken()))
                .delete()
                .build();
    }

    private static final class TokenExpiredInterceptor implements Interceptor {

        private Context mContext;

        TokenExpiredInterceptor(Context context) {
            mContext = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED && mContext != null && PrefUtils.isSignedIn(mContext)) {
                PrefUtils.signOut(mContext);
            }
            return response;
        }

        public void setContext(Context context) {
            mContext = context;
        }
    }
}
