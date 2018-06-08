package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.rules.Rules;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SavedRules implements SavedRulesService {

    private final Context     mContext;
    private final List<Rules> mSavedRulesList;
    private       Rules       mSavedRules;

    public SavedRules(Context context) {
        mContext = context;
        mSavedRulesList = new ArrayList<>();
    }

    @Override
    public void loadSavedRules() {
        mSavedRules = null;
        readSavedRules();
        syncRulesOnline();
    }

    @Override
    public List<Rules> getSavedRules() {
        return new ArrayList<>(mSavedRulesList);
    }

    @Override
    public Rules getSavedRules(String rulesName) {
        Rules matching = null;

        for (Rules savedRules : mSavedRulesList) {
            if (savedRules.getName().equals(rulesName)) {
                matching = savedRules;
            }
        }

        return matching;
    }

    @Override
    public void createRules() {
        mSavedRules = Rules.defaultUniversalRules();
        mSavedRules.setName("");
        mSavedRules.setDate(System.currentTimeMillis());
    }

    @Override
    public void createRules(GameType gameType) {
        switch (gameType) {
            case INDOOR:
                mSavedRules = Rules.officialIndoorRules();
                break;
            case INDOOR_4X4:
                mSavedRules = Rules.defaultIndoor4x4Rules();
                break;
            case BEACH:
                mSavedRules = Rules.officialBeachRules();
                break;
            default:
                mSavedRules = Rules.defaultUniversalRules();
                break;
        }
        mSavedRules.setName("");
        mSavedRules.setDate(System.currentTimeMillis());
    }

    @Override
    public void editRules(String rulesName) {
        mSavedRules = getSavedRules(rulesName);
        mSavedRulesList.remove(mSavedRules);
    }

    @Override
    public Rules getCurrentRules() {
        return mSavedRules;
    }

    @Override
    public void saveCurrentRules() {
        mSavedRules.setDate(System.currentTimeMillis());
        mSavedRulesList.add(mSavedRules);
        writeSavedRules();
        pushRulesOnline(mSavedRules);
        mSavedRules = null;
    }

    @Override
    public void deleteSavedRules(String rulesName) {
        if (mSavedRules != null && mSavedRules.getName().equals(rulesName)) {
            deleteRulesOnline(mSavedRules);
            writeSavedRules();
            mSavedRules = null;
        }
    }

    @Override
    public void deleteAllSavedRules() {
        mSavedRulesList.clear();
        writeSavedRules();
        deleteAllRulesOnline();
    }

    @Override
    public void createAndSaveRulesFrom(Rules rules) {
        if (rules.getName().length() > 0
                && getSavedRules(rules.getName()) == null
                && !rules.getName().equals(Rules.officialBeachRules().getName())
                && !rules.getName().equals(Rules.officialIndoorRules().getName())
                && !rules.getName().equals(Rules.defaultIndoor4x4Rules().getName())
                && !rules.getName().equals(Rules.defaultUniversalRules().getName())) {
            createRules();
            getCurrentRules().setAll(rules);
            saveCurrentRules();
        }
    }

    // Read saved rules

    private void readSavedRules() {
        Log.i("VBR-SavedRules", String.format("Read saved rules from %s", SAVED_RULES_FILE));

        try {
            FileInputStream inputStream = mContext.openFileInput(SAVED_RULES_FILE);
            mSavedRulesList.clear();
            mSavedRulesList.addAll(readRulesStream(inputStream));
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("VBR-SavedRules", String.format("%s saved rules file does not yet exist", SAVED_RULES_FILE));
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-SavedRules", "Exception while reading rules", e);
        }
    }

    public static List<Rules> readRulesStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RULES_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    private List<Rules> readRules(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RULES_LIST_TYPE);
    }

    // Write saved rules

    private void writeSavedRules() {
        Log.i("VBR-SavedRules", String.format("Write saved rules into %s", SAVED_RULES_FILE));
        try {
            FileOutputStream outputStream = mContext.openFileOutput(SAVED_RULES_FILE, Context.MODE_PRIVATE);
            writeRulesStream(outputStream, mSavedRulesList);
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-SavedRules", "Exception while writing rules", e);
        }
    }

    public static void writeRulesStream(OutputStream outputStream, List<Rules> rules) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(rules, JsonIOUtils.RULES_LIST_TYPE, writer);
        writer.close();
    }

    private String writeRules(Rules rules) {
        return JsonIOUtils.GSON.toJson(rules, JsonIOUtils.RULES_TYPE);
    }

    // Web

    private void syncRules(List<Rules> remoteRulesList) {
        for (Rules localRules : mSavedRulesList) {
            boolean foundRemoteVersion = false;

            for (Rules remoteRules : remoteRulesList) {
                if (localRules.getName().equals(remoteRules.getName())) {
                    foundRemoteVersion = true;

                    if (localRules.getDate() < remoteRules.getDate()) {
                        localRules.setAll(remoteRules);
                    } else if (localRules.getDate() > remoteRules.getDate()) {
                        pushRulesOnline(localRules);
                    }
                }
            }

            if (!foundRemoteVersion) {
                pushRulesOnline(localRules);
            }
        }

        for (Rules remoteRules : remoteRulesList) {
            boolean foundLocalVersion = false;

            for (Rules localRules : mSavedRulesList) {
                if (localRules.getName().equals(remoteRules.getName())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                mSavedRulesList.add(remoteRules);
            }
        }

        writeSavedRules();
    }

    private void syncRulesOnline() {
        if (PrefUtils.isPrefOnlineRecordingEnabled(mContext) && PrefUtils.isSignedIn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("userId", PrefUtils.getUserId(mContext));
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_RULES_URL + parameters, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            List<Rules> rulesList = readRules(response);
                            syncRules(rulesList);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e("VBR-SavedRules", String.format(Locale.getDefault(), "Error %d while updating rules", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void pushRulesOnline(Rules rules) {
        if (PrefUtils.isPrefOnlineRecordingEnabled(mContext) && PrefUtils.isSignedIn(mContext)) {
            rules.setUserId(PrefUtils.getUserId(mContext));
            final byte[] bytes = writeRules(rules).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, WebUtils.USER_RULES_URL, bytes,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, WebUtils.USER_RULES_URL, bytes,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {}
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                if (error.networkResponse != null) {
                                                    Log.e("VBR-SavedRules", String.format(Locale.getDefault(), "Error %d while creating rules", error.networkResponse.statusCode));
                                                }
                                            }
                                        }
                                );
                                WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
                            } else {
                                if (error.networkResponse != null) {
                                    Log.e("VBR-SavedRules", String.format(Locale.getDefault(), "Error %d while updating rules", error.networkResponse.statusCode));
                                }
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteRulesOnline(Rules rules) {
        if (PrefUtils.isPrefOnlineRecordingEnabled(mContext) && PrefUtils.isSignedIn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("userId", PrefUtils.getUserId(mContext));
            params.put("name", rules.getName());
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_RULES_URL + parameters, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e("VBR-SavedRules", String.format(Locale.getDefault(), "Error %d while deleting rules", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllRulesOnline() {
        if (PrefUtils.isPrefOnlineRecordingEnabled(mContext) && PrefUtils.isSignedIn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("userId", PrefUtils.getUserId(mContext));
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_RULES_URL + parameters, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e("VBR-SavedRules", String.format(Locale.getDefault(), "Error %d while deleting all rules", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }
}
