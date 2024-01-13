package com.tonkar.volleyballreferee.engine.api;

import android.content.Context;

import com.tonkar.volleyballreferee.engine.PrefUtils;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.*;

final class TokenExpiredInterceptor implements Interceptor {

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
