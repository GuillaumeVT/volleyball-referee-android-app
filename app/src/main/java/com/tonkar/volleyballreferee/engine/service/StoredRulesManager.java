package com.tonkar.volleyballreferee.engine.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.*;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.database.VbrRepository;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

import okhttp3.*;

public class StoredRulesManager implements StoredRulesService {

    private final Context       mContext;
    private final VbrRepository mRepository;

    public StoredRulesManager(Context context) {
        mContext = context;
        mRepository = new VbrRepository(mContext);
    }

    @Override
    public List<ApiRulesSummary> listRules() {
        return mRepository.listRules();
    }

    @Override
    public List<ApiRulesSummary> listRules(GameType kind) {
        List<ApiRulesSummary> rulesList = new ArrayList<>();
        rulesList.add(Rules.getDefaultRules(kind));
        rulesList.addAll(mRepository.listRules(kind));
        return rulesList;
    }

    @Override
    public ApiRules getRules(String id) {
        ApiRules rules = Rules.getDefaultRules(id);

        if (rules == null) {
            rules = mRepository.getRules(id);
        }

        return rules;
    }

    @Override
    public ApiRules getRules(GameType kind, String rulesName) {
        return mRepository.getRules(rulesName, kind);
    }

    @Override
    public Rules createRules(GameType gameType) {
        final Rules rules;

        switch (gameType) {
            case INDOOR_4X4:
                rules = Rules.defaultIndoor4x4Rules();
                break;
            case BEACH:
                rules = Rules.officialBeachRules();
                break;
            case SNOW:
                rules = Rules.officialSnowRules();
                break;
            case INDOOR:
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
        mRepository.insertRules(rules, false, false);
        pushRulesToServer(rules, create);
    }

    @Override
    public void deleteRules(final String id) {
        mRepository.deleteRules(id);
        deleteRulesOnServer(id);
    }

    @Override
    public void deleteRules(Set<String> ids, DataSynchronizationListener listener) {
        mRepository.deleteRules(ids);
        for (String id : ids) {
            deleteRulesOnServer(id);
        }
        if (listener != null) {
            listener.onSynchronizationSucceeded();
        }
    }

    @Override
    public void createAndSaveRulesFrom(Rules rules) {
        if (rules.getName().length() > 1 && !rules.getKind().equals(GameType.TIME) && !rules
                .getName()
                .equals(Rules.DEFAULT_BEACH_NAME) && !rules.getName().equals(Rules.DEFAULT_INDOOR_NAME) && !rules
                .getName()
                .equals(Rules.DEFAULT_INDOOR_4X4_NAME) && !rules.getName().equals(Rules.DEFAULT_SNOW_NAME) && !rules
                .getId()
                .equals(Rules.DEFAULT_BEACH_ID) && !rules.getId().equals(Rules.DEFAULT_INDOOR_ID) && !rules
                .getId()
                .equals(Rules.DEFAULT_INDOOR_4X4_ID) && !rules.getId().equals(Rules.DEFAULT_SNOW_ID) && mRepository.countRules(
                rules.getName(), rules.getKind()) == 0 && mRepository.countRules(rules.getId()) == 0) {
            saveRules(rules, true);
        }
    }

    public static List<ApiRules> readRulesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return JsonConverters.GSON.fromJson(reader, new TypeToken<List<ApiRules>>() {}.getType());
        }
    }

    public static void writeRulesStream(OutputStream outputStream, List<ApiRules> rules) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        JsonConverters.GSON.toJson(rules, new TypeToken<List<ApiRules>>() {}.getType(), writer);
        writer.close();
    }

    // Web

    @Override
    public void syncRules() {
        syncRules(null);
    }

    @Override
    public void syncRules(final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().getRulesList(mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            List<ApiRulesSummary> rulesList = JsonConverters.GSON.fromJson(body.string(),
                                                                                           new TypeToken<List<ApiRulesSummary>>() {}.getType());
                            syncRules(rulesList, listener);
                        }
                    } else {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while synchronising rules", response.code()));
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
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
                mRepository.insertRules(rules, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
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
                    break;
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
            VbrApi.getInstance().getRules(remoteRule.getId(), mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            ApiRules rules = JsonConverters.GSON.fromJson(body.string(), ApiRules.class);
                            mRepository.insertRules(rules, true, false);
                            downloadRulesRecursive(remoteRules, listener);
                        }
                    } else {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while synchronising rules", response.code()));
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }

    private void pushRulesToServer(final ApiRules rules, boolean create) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().upsertRules(rules, create, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED || response.code() == HttpURLConnection.HTTP_OK) {
                        mRepository.insertRules(rules, true, false);
                    } else {
                        Log.e(Tags.STORED_RULES, String.format(Locale.getDefault(), "Error %d while sending rules", response.code()));
                    }
                }
            });
        }
    }

    private void deleteRulesOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().deleteRules(id, mContext, new Callback() {
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
