package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.RulesEntity;
import com.tonkar.volleyballreferee.business.data.db.SyncEntity;
import com.tonkar.volleyballreferee.business.web.Authentication;
import com.tonkar.volleyballreferee.business.web.JsonStringRequest;
import com.tonkar.volleyballreferee.business.web.WebUtils;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.rules.Rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private final Context mContext;
    private       Rules   mCurrentRules;

    public SavedRules(Context context) {
        mContext = context;
    }

    @Override
    public List<Rules> getSavedRules() {
        List<String> jsonRulesList = AppDatabase.getInstance(mContext).rulesDao().getAllContents();
        List<Rules> rulesList = new ArrayList<>();

        for (String jsonRules : jsonRulesList) {
            rulesList.add(readRules(jsonRules));
        }

        return rulesList;
    }

    @Override
    public Rules getSavedRules(String rulesName) {
        String jsonRules = AppDatabase.getInstance(mContext).rulesDao().findContentByName(rulesName);
        return readRules(jsonRules);
    }

    @Override
    public void createRules() {
        mCurrentRules = Rules.defaultUniversalRules();
        mCurrentRules.setName("");
        mCurrentRules.setDate(System.currentTimeMillis());
    }

    @Override
    public void createRules(GameType gameType) {
        switch (gameType) {
            case INDOOR:
                mCurrentRules = Rules.officialIndoorRules();
                break;
            case INDOOR_4X4:
                mCurrentRules = Rules.defaultIndoor4x4Rules();
                break;
            case BEACH:
                mCurrentRules = Rules.officialBeachRules();
                break;
            default:
                mCurrentRules = Rules.defaultUniversalRules();
                break;
        }
        mCurrentRules.setName("");
        mCurrentRules.setDate(System.currentTimeMillis());
    }

    @Override
    public void editRules(String rulesName) {
        mCurrentRules = getSavedRules(rulesName);
    }

    @Override
    public Rules getCurrentRules() {
        return mCurrentRules;
    }

    @Override
    public void saveCurrentRules() {
        mCurrentRules.setDate(System.currentTimeMillis());
        insertRulesIntoDb(mCurrentRules);
        pushRulesOnline(mCurrentRules);
        mCurrentRules = null;
    }

    @Override
    public void cancelCurrentRules() {
        mCurrentRules = null;
    }

    @Override
    public void deleteSavedRules(final String rulesName) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).rulesDao().deleteByName(rulesName);
                deleteRulesOnline(rulesName);
                mCurrentRules = null;
            }
        }.start();
    }

    @Override
    public void deleteAllSavedRules() {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).rulesDao().deleteAll();
                deleteAllRulesOnline();
            }
        }.start();
    }

    @Override
    public void createAndSaveRulesFrom(Rules rules) {
        if (rules.getName().length() > 0
                && AppDatabase.getInstance(mContext).rulesDao().countByName(rules.getName()) == 0
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

    @Override
    public void migrateSavedRules() {
        String filename = "device_saved_rules.json";
        File rulesFile = mContext.getFileStreamPath(filename);

        if (rulesFile != null && rulesFile.exists()) {
            Log.i(Tags.SAVED_RULES, String.format("Migrate saved rules from %s", filename));

            try {
                FileInputStream inputStream = mContext.openFileInput(filename);
                List<Rules> rulesList = readRulesStream(inputStream);
                inputStream.close();

                final List<RulesEntity> rulesEntities = new ArrayList<>();

                for (Rules rules : rulesList) {
                    rulesEntities.add(new RulesEntity(rules.getName(), writeRules(rules)));
                }

                new Thread() {
                    public void run() {
                        AppDatabase.getInstance(mContext).rulesDao().insertAll(rulesEntities);
                        syncRulesOnline();
                    }
                }.start();

                mContext.deleteFile(filename);
            } catch (FileNotFoundException e) {
                Log.i(Tags.SAVED_RULES, String.format("%s saved rules file does not exist", filename));
            } catch (JsonParseException | IOException e) {
                Log.e(Tags.SAVED_RULES, "Exception while reading rules", e);
            }
        }
    }

    @Override
    public boolean hasSavedRules() {
        return AppDatabase.getInstance(mContext).rulesDao().count() > 0;
    }

    public static List<Rules> readRulesStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RULES_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    private Rules readRules(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RULES_TYPE);
    }

    private List<Rules> readRulesList(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RULES_LIST_TYPE);
    }

    // Write saved rules

    private void insertRulesIntoDb(final Rules rules) {
        new Thread() {
            public void run() {
                String json = writeRules(rules);
                RulesEntity rulesEntity = new RulesEntity(rules.getName(), json);
                AppDatabase.getInstance(mContext).rulesDao().insert(rulesEntity);
            }
        }.start();
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
        List<Rules> localRulesList = getSavedRules();

        for (Rules localRules : localRulesList) {
            boolean foundRemoteVersion = false;

            for (Rules remoteRules : remoteRulesList) {
                if (localRules.getName().equals(remoteRules.getName())) {
                    foundRemoteVersion = true;

                    if (localRules.getDate() < remoteRules.getDate()) {
                        localRules.setAll(remoteRules);
                        insertRulesIntoDb(localRules);
                    } else if (localRules.getDate() > remoteRules.getDate()) {
                        pushRulesOnline(localRules);
                    }
                }
            }

            if (!foundRemoteVersion) {
                if (isSynced(localRules.getName())) {
                    // if the rules were synced, then they were deleted from the server and they must be deleted locally
                    deleteSavedRules(localRules.getName());
                } else {
                    // if the rules were not synced, then they are missing from the server because sending them must have failed, so send them again
                    pushRulesOnline(localRules);
                }
            }
        }

        for (Rules remoteRules : remoteRulesList) {
            boolean foundLocalVersion = false;

            for (Rules localRules : localRulesList) {
                if (localRules.getName().equals(remoteRules.getName())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                if (isSynced(remoteRules.getName())) {
                    // if the rules were synced, then sending the deletion to the server must have failed, so send the deletion again
                    deleteRulesOnline(remoteRules.getName());
                } else {
                    // if the rules were not synced, then they were added on the server and they must be added locally
                    insertRulesIntoDb(remoteRules);
                    pushRulesOnline(remoteRules);
                }
            }
        }
    }

    @Override
    public void syncRulesOnline() {
        syncRulesOnline(null);
    }

    @Override
    public void syncRulesOnline(final DataSynchronizationListener listener) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_RULES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            List<Rules> rulesList = readRulesList(response);
                            syncRules(rulesList);
                            if (listener != null){
                                listener.onSynchronizationSucceeded();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(Tags.SAVED_RULES, String.format(Locale.getDefault(), "Error %d while synchronising rules", error.networkResponse.statusCode));
                            }
                            if (listener != null){
                                listener.onSynchronizationFailed();
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            if (listener != null){
                listener.onSynchronizationFailed();
            }
        }
    }

    private void pushRulesOnline(final Rules rules) {
        if (PrefUtils.isSyncOn(mContext)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            rules.setUserId(authentication.getUserId());
            final byte[] bytes = writeRules(rules).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, WebUtils.USER_RULES_API_URL, bytes, authentication,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            insertSyncIntoDb(rules.getName());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, WebUtils.USER_RULES_API_URL, bytes, authentication,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                insertSyncIntoDb(rules.getName());
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                if (error.networkResponse != null) {
                                                    Log.e(Tags.SAVED_RULES, String.format(Locale.getDefault(), "Error %d while creating rules", error.networkResponse.statusCode));
                                                }
                                            }
                                        }
                                );
                                WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
                            } else {
                                if (error.networkResponse != null) {
                                    Log.e(Tags.SAVED_RULES, String.format(Locale.getDefault(), "Error %d while creating rules", error.networkResponse.statusCode));
                                }
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteRulesOnline(final String rulesName) {
        if (PrefUtils.isSyncOn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("name", rulesName);
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_RULES_API_URL + parameters, new byte[0], PrefUtils.getAuthentication(mContext),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            AppDatabase.getInstance(mContext).syncDao().deleteByItemAndType(SyncEntity.createRulesItem(rulesName), SyncEntity.RULES_ENTITY);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(Tags.SAVED_RULES, String.format(Locale.getDefault(), "Error %d while deleting rules", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllRulesOnline() {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_RULES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            AppDatabase.getInstance(mContext).syncDao().deleteByType(SyncEntity.RULES_ENTITY);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(Tags.SAVED_RULES, String.format(Locale.getDefault(), "Error %d while deleting all rules", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private boolean isSynced(String rulesName) {
        return AppDatabase.getInstance(mContext).syncDao().countByItemAndType(SyncEntity.createRulesItem(rulesName), SyncEntity.RULES_ENTITY) > 0;
    }

    private void insertSyncIntoDb(final String rulesName) {
        AppDatabase.getInstance(mContext).syncDao().insert(SyncEntity.createRulesSyncEntity(rulesName));
    }
}
