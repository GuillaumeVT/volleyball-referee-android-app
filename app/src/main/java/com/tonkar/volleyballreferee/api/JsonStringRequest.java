package com.tonkar.volleyballreferee.api;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JsonStringRequest extends StringRequest {

    private final byte[]         mBody;
    private final Authentication mAuthentication;

    public JsonStringRequest(int method, String url, byte[] body, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        this(method, url, body, null, listener, errorListener);
    }

    public JsonStringRequest(int method, String url, byte[] body, Authentication authentication, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mBody = body;
        mAuthentication = authentication;
        setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> params = new HashMap<>();
        params.put("Content-Type", "application/json");
        if (mAuthentication != null && !mAuthentication.getUserId().equals(Authentication.VBR_USER_ID) && !mAuthentication.getToken().isEmpty()) {
            params.put("Authorization", String.format("Bearer %s", mAuthentication.getToken()));
            params.put("AuthenticationProvider", mAuthentication.getProvider().toString());
        }

        return params;
    }

    @Override
    public byte[] getBody() {
        return mBody;
    }

    public static String getParameters(Map<String, String> parameters) {
        StringBuilder builder = new StringBuilder();

        if (!parameters.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (first) {
                    builder.append('?');
                } else {
                    builder.append('&');
                }

                builder.append(entry.getKey()).append('=').append(entry.getValue());
                first = false;
            }
        }
        return builder.toString();
    }
}
