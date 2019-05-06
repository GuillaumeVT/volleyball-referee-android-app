package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.RulesEntity;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredRulesService;
import com.tonkar.volleyballreferee.business.rules.Rules;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.*;

public class StoredRules implements StoredRulesService {

    private final Context mContext;

    public StoredRules(Context context) {
        mContext = context;
    }

    @Override
    public List<ApiRulesDescription> listRules() {
        return AppDatabase.getInstance(mContext).rulesDao().listRules();
    }

    @Override
    public List<ApiRulesDescription> listRules(GameType kind) {
        return AppDatabase.getInstance(mContext).rulesDao().listRulesByKind(kind);
    }

    @Override
    public ApiRules getRules(String id) {
        String jsonRules = AppDatabase.getInstance(mContext).rulesDao().findContentById(id);
        return readRules(jsonRules);
    }

    @Override
    public ApiRules getRules(GameType kind, String rulesName) {
        String jsonRules = AppDatabase.getInstance(mContext).rulesDao().findContentByNameAndKind(rulesName, kind);
        return readRules(jsonRules);
    }

    @Override
    public Rules createRules(GameType gameType) {
        final Rules rules;

        switch (gameType) {
            case INDOOR:
                rules = Rules.officialIndoorRules();
                break;
            case INDOOR_4X4:
                rules = Rules.defaultIndoor4x4Rules();
                break;
            case BEACH:
                rules = Rules.officialBeachRules();
                break;
            default:
                rules = Rules.officialIndoorRules();
                break;
        }

        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();

        rules.setId(UUID.randomUUID().toString());
        rules.setCreatedBy(PrefUtils.getAuthentication(mContext).getUserId());
        rules.setCreatedAt(utcTime);
        rules.setUpdatedAt(utcTime);
        rules.setName("");

        return rules;
    }

    @Override
    public void saveRules(Rules rules) {
        rules.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        insertRulesIntoDb(rules, false, false);
        pushRulesToServer(rules);
    }

    @Override
    public void deleteRules(final String id) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).rulesDao().deleteById(id);
                deleteRulesOnServer(id);
            }
        }.start();
    }

    @Override
    public void deleteAllRules() {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).rulesDao().deleteAll();
                deleteAllRulesServer();
            }
        }.start();
    }

    @Override
    public void createAndSaveRulesFrom(Rules rules) {
        if (rules.getName().length() > 1
                && AppDatabase.getInstance(mContext).rulesDao().countByNameAndKind(rules.getName(), rules.getKind()) == 0
                && !rules.getName().equals(Rules.officialBeachRules().getName())
                && !rules.getName().equals(Rules.officialIndoorRules().getName())
                && !rules.getName().equals(Rules.defaultIndoor4x4Rules().getName())) {
            saveRules(rules);
        }
    }

    // Read saved rules

    @Override
    public boolean hasRules() {
        return AppDatabase.getInstance(mContext).rulesDao().count() > 0;
    }

    public static List<ApiRules> readRulesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RULES_LIST_TYPE);
        }
    }

    @Override
    public ApiRules readRules(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RULES_TYPE);
    }

    private List<ApiRulesDescription> readRulesList(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RULES_DESCRIPTION_LIST_TYPE);
    }

    // Write saved rules

    private void insertRulesIntoDb(final ApiRules apiRules, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            String json = writeRules(apiRules);
            RulesEntity rulesEntity = new RulesEntity();
            rulesEntity.setId(apiRules.getId());
            rulesEntity.setCreatedBy(apiRules.getCreatedBy());
            rulesEntity.setCreatedAt(apiRules.getCreatedAt());
            rulesEntity.setUpdatedAt(apiRules.getUpdatedAt());
            rulesEntity.setKind(apiRules.getKind());
            rulesEntity.setName(apiRules.getName());
            rulesEntity.setSynced(synced);
            rulesEntity.setContent(json);
            AppDatabase.getInstance(mContext).rulesDao().insert(rulesEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            new Thread(runnable).start();
        }
    }

    public static void writeRulesStream(OutputStream outputStream, List<ApiRules> rules) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(rules, JsonIOUtils.RULES_LIST_TYPE, writer);
        writer.close();
    }

    @Override
    public String writeRules(ApiRules rules) {
        return JsonIOUtils.GSON.toJson(rules, JsonIOUtils.RULES_TYPE);
    }

    // Web

    @Override
    public void syncRules() {
        syncRules(null);
    }

    @Override
    public void syncRules(final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.RULES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiRulesDescription> rulesList = readRulesList(response);
                        syncRules(rulesList, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while synchronising rules", error.networkResponse.statusCode));
                        }
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            if (listener != null){
                listener.onSynchronizationFailed();
            }
        }
    }

    private void syncRules(List<ApiRulesDescription> remoteRulesList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getAuthentication(mContext).getUserId();
        List<ApiRulesDescription> localRulesList = listRules();
        Queue<ApiRulesDescription> remoteRulesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiRulesDescription localRules : localRulesList) {
            if (localRules.getCreatedBy().equals(Authentication.VBR_USER_ID)) {
                ApiRules rules = getRules(localRules.getId());
                rules.setCreatedBy(userId);
                rules.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                insertRulesIntoDb(rules, false, true);
                afterPurchase = true;
            }
        }

        if(afterPurchase) {
            localRulesList = listRules();
        }

        for (ApiRulesDescription localRules : localRulesList) {
            boolean foundRemoteVersion = false;

            for (ApiRulesDescription remoteRules : remoteRulesList) {
                if (localRules.getId().equals(remoteRules.getId())) {
                    foundRemoteVersion = true;

                    if (localRules.getUpdatedAt() < remoteRules.getUpdatedAt()) {
                        remoteRulesToDownload.add(remoteRules);
                    } else if (localRules.getUpdatedAt() > remoteRules.getUpdatedAt()) {
                        ApiRules rules = getRules(localRules.getId());
                        pushRulesToServer(rules);
                    }
                }
            }

            if (!foundRemoteVersion) {
                if (localRules.isSynced()) {
                    // if the rules were synced, then they were deleted from the server and they must be deleted locally
                    deleteRules(localRules.getId());
                } else {
                    // if the rules were not synced, then they are missing from the server because sending them must have failed, so send them again
                    ApiRules rules = getRules(localRules.getId());
                    pushRulesToServer(rules);
                }
            }
        }

        for (ApiRulesDescription remoteRules : remoteRulesList) {
            boolean foundLocalVersion = false;

            for (ApiRulesDescription localRules : localRulesList) {
                if (localRules.getId().equals(remoteRules.getId())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                remoteRulesToDownload.add(remoteRules);
            }
        }

        downloadRulesRecursive(remoteRulesToDownload, listener);
    }

    private void downloadRulesRecursive(final Queue<ApiRulesDescription> remoteRules, final DataSynchronizationListener listener) {
        if (remoteRules.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiRulesDescription remoteRule = remoteRules.poll();
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, String.format(ApiUtils.RULE_API_URL, remoteRule.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        ApiRules rules = readRules(response);
                        insertRulesIntoDb(rules, true, false);
                        downloadRulesRecursive(remoteRules, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while synchronising rules", error.networkResponse.statusCode));
                        }
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void pushRulesToServer(final ApiRules rules) {
        if (PrefUtils.canSync(mContext)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            final byte[] bytes = writeRules(rules).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, ApiUtils.RULES_API_URL, bytes, authentication,
                    response -> insertRulesIntoDb(rules, true, false),
                    error -> {
                        if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                            JsonStringRequest stringRequest1 = new JsonStringRequest(Request.Method.POST, ApiUtils.RULES_API_URL, bytes, authentication,
                                    response -> insertRulesIntoDb(rules, true, false),
                                    error2 -> {
                                        if (error2.networkResponse != null) {
                                            Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while creating rules", error2.networkResponse.statusCode));
                                        }
                                    }
                            );
                            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest1);
                        } else {
                            if (error.networkResponse != null) {
                                Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while creating rules", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteRulesOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, String.format(ApiUtils.RULE_API_URL, id), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while deleting rules", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllRulesServer() {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, ApiUtils.RULES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while deleting all rules", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }
}
