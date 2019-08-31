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
import com.tonkar.volleyballreferee.engine.stored.api.ApiLeague;
import com.tonkar.volleyballreferee.engine.stored.api.ApiLeagueSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSelectedLeague;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUtils;
import com.tonkar.volleyballreferee.engine.stored.database.AppDatabase;
import com.tonkar.volleyballreferee.engine.stored.database.LeagueEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class StoredLeaguesManager implements StoredLeaguesService {

    private final Context mContext;

    public StoredLeaguesManager(Context context) {
        mContext = context;
    }

    @Override
    public List<ApiLeagueSummary> listLeagues() {
        return AppDatabase.getInstance(mContext).leagueDao().listLeagues();
    }

    @Override
    public List<ApiLeagueSummary> listLeagues(GameType kind) {
        return AppDatabase.getInstance(mContext).leagueDao().listLeaguesByKind(kind);
    }

    @Override
    public List<String> listDivisionNames(String id) {
        ApiLeague league = getLeague(id);
        return league.getDivisions();
    }

    @Override
    public ApiLeague getLeague(String id) {
        String jsonLeague = AppDatabase.getInstance(mContext).leagueDao().findContentById(id);
        return readLeague(jsonLeague);
    }

    private void saveLeague(ApiLeague league) {
        league.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        insertLeagueIntoDb(league, false, false);
        pushLeagueToServer(league);
    }

    @Override
    public void createAndSaveLeagueFrom(ApiSelectedLeague selectedLeague) {
        if (selectedLeague.getName().length() > 1 && selectedLeague.getDivision().length() > 1 && !selectedLeague.getKind().equals(GameType.TIME)) {
            long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();

            int leaguesSameName = AppDatabase.getInstance(mContext).leagueDao().countByNameAndKind(selectedLeague.getName(), selectedLeague.getKind());
            int leaguesSameId = AppDatabase.getInstance(mContext).leagueDao().countById(selectedLeague.getId());

            if (leaguesSameName == 0 && leaguesSameId == 0) {
                ApiLeague league = new ApiLeague();
                league.setAll(selectedLeague);
                saveLeague(league);
            } else if (leaguesSameName == 1 && leaguesSameId == 1) {
                ApiLeague league = getLeague(selectedLeague.getId());
                if (!league.getDivisions().contains(selectedLeague.getDivision())) {
                    league.setUpdatedAt(utcTime);
                    league.getDivisions().add(selectedLeague.getDivision());
                    insertLeagueIntoDb(league, false, false);
                }
            }
        }
    }

    @Override
    public ApiLeague getLeague(GameType kind, String leagueName) {
        String json = AppDatabase.getInstance(mContext).leagueDao().findContentByNameAndKind(leagueName, kind);
        return readLeague(json);
    }

    private ApiLeague readLeague(String json) {
        return JsonIOUtils.GSON.fromJson(json, ApiLeague.class);
    }

    private String writeLeague(ApiLeague league) {
        return JsonIOUtils.GSON.toJson(league, ApiLeague.class);
    }

    public static List<ApiLeague> readLeaguesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
            return JsonIOUtils.GSON.fromJson(reader, new TypeToken<List<ApiLeague>>(){}.getType());
        }
    }

    public static void writeLeaguesStream(OutputStream outputStream, List<ApiLeague> leagues) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(leagues, new TypeToken<List<ApiLeague>>(){}.getType(), writer);
        writer.close();
    }

    private List<ApiLeagueSummary> readLeagueList(String json) {
        return JsonIOUtils.GSON.fromJson(json, new TypeToken<List<ApiLeagueSummary>>(){}.getType());
    }

    private void insertLeagueIntoDb(final ApiLeague apiLeague, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            String json = writeLeague(apiLeague);
            LeagueEntity leagueEntity = new LeagueEntity();
            leagueEntity.setId(apiLeague.getId());
            leagueEntity.setCreatedBy(apiLeague.getCreatedBy());
            leagueEntity.setCreatedAt(apiLeague.getCreatedAt());
            leagueEntity.setUpdatedAt(apiLeague.getUpdatedAt());
            leagueEntity.setKind(apiLeague.getKind());
            leagueEntity.setName(apiLeague.getName());
            leagueEntity.setSynced(synced);
            leagueEntity.setContent(json);
            AppDatabase.getInstance(mContext).leagueDao().insert(leagueEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            new Thread(runnable).start();
        }
    }

    private void deleteLeagueFromDb(final String id) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).leagueDao().deleteById(id);
            }
        }.start();
    }

    @Override
    public void syncLeagues() {
        syncLeagues(null);
    }

    @Override
    public void syncLeagues(DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/leagues", ApiUtils.BASE_URL), PrefUtils.getAuhentication(mContext));

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
                        List<ApiLeagueSummary> leagueList = readLeagueList(response.body().string());
                        syncLeagues(leagueList, listener);
                    } else {
                        Log.e(Tags.STORED_LEAGUES, String.format(Locale.getDefault(), "Error %d while synchronising leagues", response.code()));
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

    private void syncLeagues(List<ApiLeagueSummary> remoteLeagueList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getUser(mContext).getId();
        List<ApiLeagueSummary> localLeagueList = listLeagues();
        Queue<ApiLeagueSummary> remoteLeaguesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiLeagueSummary localLeague : localLeagueList) {
            if (localLeague.getCreatedBy().equals(ApiUserSummary.VBR_USER_ID)) {
                ApiLeague league = getLeague(localLeague.getId());
                league.setCreatedBy(userId);
                league.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                insertLeagueIntoDb(league, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localLeagueList = listLeagues();
        }

        for (ApiLeagueSummary localLeague : localLeagueList) {
            boolean foundRemoteVersion = false;

            for (ApiLeagueSummary remoteLeague : remoteLeagueList) {
                if (localLeague.getId().equals(remoteLeague.getId())) {
                    foundRemoteVersion = true;
                    remoteLeaguesToDownload.add(remoteLeague);
                }
            }

            if (!foundRemoteVersion) {
                if (localLeague.isSynced()) {
                    // if the league was synced, then it was deleted from the server and it must be deleted locally
                    deleteLeagueFromDb(localLeague.getId());
                } else {
                    // if the league was not synced, then it is missing from the server because sending it must have failed, so send it again
                    ApiLeague league = getLeague(localLeague.getId());
                    pushLeagueToServer(league);
                }
            }
        }

        for (ApiLeagueSummary remoteLeague : remoteLeagueList) {
            boolean foundLocalVersion = false;

            for (ApiLeagueSummary localLeague : localLeagueList) {
                if (localLeague.getId().equals(remoteLeague.getId())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                remoteLeaguesToDownload.add(remoteLeague);
            }
        }

        downloadLeaguesRecursive(remoteLeaguesToDownload, listener);
    }

    private void downloadLeaguesRecursive(final Queue<ApiLeagueSummary> remoteLeagues, final DataSynchronizationListener listener) {
        if (remoteLeagues.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiLeagueSummary remoteLeague = remoteLeagues.poll();
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/leagues/%s", ApiUtils.BASE_URL, remoteLeague.getId()), PrefUtils.getAuhentication(mContext));

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
                        ApiLeague league = readLeague(response.body().string());
                        insertLeagueIntoDb(league, true, false);
                        downloadLeaguesRecursive(remoteLeagues, listener);
                    } else {
                        Log.e(Tags.STORED_LEAGUES, String.format(Locale.getDefault(), "Error %d while synchronising league", response.code()));
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }

    private void pushLeagueToServer(final ApiLeague league) {
        if (PrefUtils.canSync(mContext)) {
            final String leagueStr = writeLeague(league);
            Request request = ApiUtils.buildPost(String.format(Locale.US, "%s/leagues", ApiUtils.BASE_URL), leagueStr, PrefUtils.getAuhentication(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        insertLeagueIntoDb(league, true, false);
                    } else {
                        Log.e(Tags.STORED_LEAGUES, String.format(Locale.getDefault(), "Error %d while posting league", response.code()));
                    }
                }
            });
        }
    }
}
