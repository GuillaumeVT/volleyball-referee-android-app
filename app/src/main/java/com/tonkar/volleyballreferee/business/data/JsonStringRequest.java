package com.tonkar.volleyballreferee.business.data;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JsonStringRequest extends StringRequest {

    private final byte[] mBytes;

    public JsonStringRequest(int method, String url, byte[] bytes, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mBytes = bytes;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> params = new HashMap<>();
        params.put("Content-Type", "application/json");
        return params;
    }

    @Override
    public byte[] getBody() {
        return mBytes;
    }
}
