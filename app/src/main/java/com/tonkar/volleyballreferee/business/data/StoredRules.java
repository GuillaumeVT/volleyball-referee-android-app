package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

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
        List<ApiRulesDescription> rulesList = new ArrayList<>();
        rulesList.add(Rules.getDefaultRules(kind));
        rulesList.addAll(AppDatabase.getInstance(mContext).rulesDao().listRulesByKind(kind));
        return rulesList;
    }

    @Override
    public ApiRules getRules(String id) {
        ApiRules rules = Rules.getDefaultRules(id);

        if (rules == null) {
            String jsonRules = AppDatabase.getInstance(mContext).rulesDao().findContentById(id);
            rules = readRules(jsonRules);
        }

        return rules;
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
    public void saveRules(Rules rules, boolean create) {
        rules.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        insertRulesIntoDb(rules, false, false);
        pushRulesToServer(rules, create);
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
                && !rules.getKind().equals(GameType.TIME)
                && !rules.getName().equals(Rules.DEFAULT_BEACH_NAME)
                && !rules.getName().equals(Rules.DEFAULT_INDOOR_NAME)
                && !rules.getName().equals(Rules.DEFAULT_INDOOR_4X4_NAME)
                && !rules.getId().equals(Rules.DEFAULT_BEACH_ID)
                && !rules.getId().equals(Rules.DEFAULT_INDOOR_ID)
                && !rules.getId().equals(Rules.DEFAULT_INDOOR_4X4_ID)
                && AppDatabase.getInstance(mContext).rulesDao().countByNameAndKind(rules.getName(), rules.getKind()) == 0
                && AppDatabase.getInstance(mContext).rulesDao().countById(rules.getId()) == 0) {
            saveRules(rules, true);
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
            Request request = ApiUtils.buildGet(ApiUtils.RULES_API_URL, PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    if (listener != null){
                        listener.onSynchronizationFailed();
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        List<ApiRulesDescription> rulesList = readRulesList(response.body().string());
                        syncRules(rulesList, listener);
                    } else {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while synchronising rules", response.code()));
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
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
                        pushRulesToServer(rules, false);
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
                    pushRulesToServer(rules, true);
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
            Request request = ApiUtils.buildGet(String.format(ApiUtils.RULE_API_URL, remoteRule.getId()), PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    if (listener != null){
                        listener.onSynchronizationFailed();
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ApiRules rules = readRules(response.body().string());
                        insertRulesIntoDb(rules, true, false);
                        downloadRulesRecursive(remoteRules, listener);
                    } else {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while synchronising rules", response.code()));
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }

    private void pushRulesToServer(final ApiRules rules, boolean create) {
        if (PrefUtils.canSync(mContext)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            final String rulesStr = writeRules(rules);

            Request request = create ?
                    ApiUtils.buildPost(ApiUtils.RULES_API_URL, rulesStr, authentication) :
                    ApiUtils.buildPut(ApiUtils.RULES_API_URL, rulesStr, authentication);

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED || response.code() == HttpURLConnection.HTTP_OK) {
                        insertRulesIntoDb(rules, true, false);
                    } else {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while sending rules", response.code()));
                    }
                }
            });
        }
    }

    private void deleteRulesOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildDelete(String.format(ApiUtils.RULE_API_URL, id), PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while deleting rules", response.code()));
                    }
                }
            });
        }
    }

    private void deleteAllRulesServer() {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildDelete(ApiUtils.RULES_API_URL, PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while deleting all rules", response.code()));
                    }
                }
            });
        }
    }
}
