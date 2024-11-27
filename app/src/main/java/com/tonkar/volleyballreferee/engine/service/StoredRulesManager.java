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

import org.apache.commons.lang3.StringUtils;

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
    public List<RulesSummaryDto> listRules() {
        return mRepository.listRules();
    }

    @Override
    public List<RulesSummaryDto> listRules(GameType kind) {
        List<RulesSummaryDto> rulesList = new ArrayList<>();
        rulesList.add(Rules.getDefaultRules(kind));
        rulesList.addAll(mRepository.listRules(kind));
        return rulesList;
    }

    @Override
    public RulesDto getRules(String id) {
        RulesDto rules = Rules.getDefaultRules(id);

        if (rules == null) {
            rules = mRepository.getRules(id);
        }

        return rules;
    }

    @Override
    public RulesDto getRules(GameType kind, String rulesName) {
        return mRepository.getRules(rulesName, kind);
    }

    @Override
    public Rules createRules(GameType gameType) {
        final Rules rules = switch (gameType) {
            case INDOOR_4X4 -> Rules.defaultIndoor4x4Rules();
            case BEACH -> Rules.officialBeachRules();
            case SNOW -> Rules.officialSnowRules();
            default -> Rules.officialIndoorRules();
        };

        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();

        rules.setId(UUID.randomUUID().toString());
        rules.setCreatedBy(PrefUtils.getUserId(mContext));
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
        if (!ids.isEmpty()) {
            mRepository.deleteRules(ids);
            for (String id : ids) {
                deleteRulesOnServer(id);
            }
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        }
    }

    @Override
    public void createAndSaveRulesFrom(Rules rules) {
        if (rules.getName().length() > 1 && !rules.getName().equals(Rules.DEFAULT_BEACH_NAME) && !rules
                .getName()
                .equals(Rules.DEFAULT_INDOOR_NAME) && !rules.getName().equals(Rules.DEFAULT_INDOOR_4X4_NAME) && !rules
                .getName()
                .equals(Rules.DEFAULT_SNOW_NAME) && !rules.getId().equals(Rules.DEFAULT_BEACH_ID) && !rules
                .getId()
                .equals(Rules.DEFAULT_INDOOR_ID) && !rules.getId().equals(Rules.DEFAULT_INDOOR_4X4_ID) && !rules
                .getId()
                .equals(Rules.DEFAULT_SNOW_ID) && mRepository.countRules(rules.getName(), rules.getKind()) == 0 && mRepository.countRules(
                rules.getId()) == 0) {
            saveRules(rules, true);
        }
    }

    public static List<RulesDto> readRulesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return JsonConverters.GSON.fromJson(reader, new TypeToken<List<RulesDto>>() {}.getType());
        }
    }

    public static void writeRulesStream(OutputStream outputStream, List<RulesDto> rules) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        JsonConverters.GSON.toJson(rules, new TypeToken<List<RulesDto>>() {}.getType(), writer);
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
            VbrApi.getInstance(mContext).getRulesList(mContext, new Callback() {
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
                            List<RulesSummaryDto> rulesList = JsonConverters.GSON.fromJson(body.string(),
                                                                                           new TypeToken<List<RulesSummaryDto>>() {}.getType());
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

    private void syncRules(List<RulesSummaryDto> remoteRulesList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getUserId(mContext);
        List<RulesSummaryDto> localRulesList = listRules();
        Queue<RulesSummaryDto> remoteRulesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (RulesSummaryDto localRules : localRulesList) {
            if (StringUtils.isBlank(localRules.getCreatedBy())) {
                RulesDto rules = getRules(localRules.getId());
                rules.setCreatedBy(userId);
                rules.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                mRepository.insertRules(rules, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localRulesList = listRules();
        }

        for (RulesSummaryDto localRules : localRulesList) {
            boolean foundRemoteVersion = false;

            for (RulesSummaryDto remoteRules : remoteRulesList) {
                if (localRules.getId().equals(remoteRules.getId())) {
                    foundRemoteVersion = true;

                    if (localRules.getUpdatedAt() < remoteRules.getUpdatedAt()) {
                        remoteRulesToDownload.add(remoteRules);
                    } else if (localRules.getUpdatedAt() > remoteRules.getUpdatedAt()) {
                        RulesDto rules = getRules(localRules.getId());
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
                    RulesDto rules = getRules(localRules.getId());
                    pushRulesToServer(rules, true);
                }
            }
        }

        for (RulesSummaryDto remoteRules : remoteRulesList) {
            boolean foundLocalVersion = false;

            for (RulesSummaryDto localRules : localRulesList) {
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

    private void downloadRulesRecursive(final Queue<RulesSummaryDto> remoteRules, final DataSynchronizationListener listener) {
        if (remoteRules.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            RulesSummaryDto remoteRule = remoteRules.poll();
            VbrApi.getInstance(mContext).getRules(remoteRule.getId(), mContext, new Callback() {
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
                            RulesDto rules = JsonConverters.GSON.fromJson(body.string(), RulesDto.class);
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

    private void pushRulesToServer(final RulesDto rules, boolean create) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).upsertRules(rules, create, mContext, new Callback() {
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
            VbrApi.getInstance(mContext).deleteRules(id, mContext, new Callback() {
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
