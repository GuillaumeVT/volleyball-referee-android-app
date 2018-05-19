package com.tonkar.volleyballreferee.business.data;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JsonStringRequest extends StringRequest {

    private final byte[]              mBody;

    public JsonStringRequest(int method, String url, byte[] body, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mBody = body;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> params = new HashMap<>();
        params.put("Content-Type", "application/json");
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
