package com.tonkar.volleyballreferee.engine.stored;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRulesSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserToken;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUtils;
import com.tonkar.volleyballreferee.engine.stored.database.AppDatabase;
import com.tonkar.volleyballreferee.engine.stored.database.RulesEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class StoredRulesManager implements StoredRulesService {

    private final Context mContext;

    public StoredRulesManager(Context context) {
        mContext = context;
    }

    @Override
    public List<ApiRulesSummary> listRules() {
        return AppDatabase.getInstance(mContext).rulesDao().listRules();
    }

    @Override
    public List<ApiRulesSummary> listRules(GameType kind) {
        List<ApiRulesSummary> rulesList = new ArrayList<>();
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
        rules.setCreatedBy(PrefUtils.getUser(mContext).getId());
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
    public void deleteRules(Set<String> ids, DataSynchronizationListener listener) {
        new Thread() {
            public void run() {
                for (String id : ids) {
                    AppDatabase.getInstance(mContext).rulesDao().deleteById(id);
                    deleteRulesOnServer(id);
                    if (listener != null) {
                        listener.onSynchronizationSucceeded();
                    }
                }
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
            return JsonIOUtils.GSON.fromJson(reader, new TypeToken<List<ApiRules>>(){}.getType());
        }
    }

    @Override
    public ApiRules readRules(String json) {
        return JsonIOUtils.GSON.fromJson(json, ApiRules.class);
    }

    private List<ApiRulesSummary> readRulesList(String json) {
        return JsonIOUtils.GSON.fromJson(json, new TypeToken<List<ApiRulesSummary>>(){}.getType());
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
        JsonIOUtils.GSON.toJson(rules, new TypeToken<List<ApiRules>>(){}.getType(), writer);
        writer.close();
    }

    @Override
    public String writeRules(ApiRules rules) {
        return JsonIOUtils.GSON.toJson(rules, ApiRules.class);
    }

    // Web

    @Override
    public void syncRules() {
        syncRules(null);
    }

    @Override
    public void syncRules(final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/rules", ApiUtils.BASE_URL), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
                        List<ApiRulesSummary> rulesList = readRulesList(response.body().string());
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

    private void syncRules(List<ApiRulesSummary> remoteRulesList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getUser(mContext).getId();
        List<ApiRulesSummary> localRulesList = listRules();
        Queue<ApiRulesSummary> remoteRulesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiRulesSummary localRules : localRulesList) {
            if (localRules.getCreatedBy().equals(ApiUserSummary.VBR_USER_ID)) {
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

        for (ApiRulesSummary localRules : localRulesList) {
            boolean foundRemoteVersion = false;

            for (ApiRulesSummary remoteRules : remoteRulesList) {
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

        for (ApiRulesSummary remoteRules : remoteRulesList) {
            boolean foundLocalVersion = false;

            for (ApiRulesSummary localRules : localRulesList) {
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

    private void downloadRulesRecursive(final Queue<ApiRulesSummary> remoteRules, final DataSynchronizationListener listener) {
        if (remoteRules.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiRulesSummary remoteRule = remoteRules.poll();
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/rules/%s", ApiUtils.BASE_URL, remoteRule.getId()), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            final ApiUserToken userToken = PrefUtils.getUserToken(mContext);
            final String rulesStr = writeRules(rules);

            Request request = create ?
                    ApiUtils.buildPost(String.format(Locale.US, "%s/rules", ApiUtils.BASE_URL), rulesStr, userToken) :
                    ApiUtils.buildPut(String.format(Locale.US, "%s/rules", ApiUtils.BASE_URL), rulesStr, userToken);

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            Request request = ApiUtils.buildDelete(String.format(Locale.US, "%s/rules/%s", ApiUtils.BASE_URL, id), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
}
