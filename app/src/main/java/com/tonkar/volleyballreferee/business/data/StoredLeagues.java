package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.LeagueEntity;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredLeaguesService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

public class StoredLeagues implements StoredLeaguesService {

    private final Context mContext;

    public StoredLeagues(Context context) {
        mContext = context;
    }

    @Override
    public List<ApiLeagueDescription> listLeagues() {
        return AppDatabase.getInstance(mContext).leagueDao().listLeagues();
    }

    @Override
    public List<ApiLeagueDescription> listLeagues(GameType kind) {
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
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.LEAGUE_TYPE);
    }

    private String writeLeague(ApiLeague league) {
        return JsonIOUtils.GSON.toJson(league, JsonIOUtils.LEAGUE_TYPE);
    }

    public static List<ApiLeague> readLeaguesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.LEAGUE_LIST_TYPE);
        }
    }

    public static void writeLeaguesStream(OutputStream outputStream, List<ApiLeague> leagues) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(leagues, JsonIOUtils.LEAGUE_LIST_TYPE, writer);
        writer.close();
    }

    private List<ApiLeagueDescription> readLeagueList(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.LEAGUE_DESCRIPTION_LIST_TYPE);
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
            Request request = ApiUtils.buildGet(ApiUtils.LEAGUES_API_URL, PrefUtils.getAuthentication(mContext));

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
                        List<ApiLeagueDescription> leagueList = readLeagueList(response.body().string());
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

    private void syncLeagues(List<ApiLeagueDescription> remoteLeagueList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getAuthentication(mContext).getUserId();
        List<ApiLeagueDescription> localLeagueList = listLeagues();
        Queue<ApiLeagueDescription> remoteLeaguesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiLeagueDescription localLeague : localLeagueList) {
            if (localLeague.getCreatedBy().equals(Authentication.VBR_USER_ID)) {
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

        for (ApiLeagueDescription localLeague : localLeagueList) {
            boolean foundRemoteVersion = false;

            for (ApiLeagueDescription remoteLeague : remoteLeagueList) {
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

        for (ApiLeagueDescription remoteLeague : remoteLeagueList) {
            boolean foundLocalVersion = false;

            for (ApiLeagueDescription localLeague : localLeagueList) {
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

    private void downloadLeaguesRecursive(final Queue<ApiLeagueDescription> remoteLeagues, final DataSynchronizationListener listener) {
        if (remoteLeagues.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiLeagueDescription remoteLeague = remoteLeagues.poll();
            Request request = ApiUtils.buildGet(String.format(ApiUtils.LEAGUE_API_URL, remoteLeague.getId()), PrefUtils.getAuthentication(mContext));

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
            Request request = ApiUtils.buildPost(ApiUtils.LEAGUES_API_URL, leagueStr, PrefUtils.getAuthentication(mContext));

            ApiUtils.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
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
