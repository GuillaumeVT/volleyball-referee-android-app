package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
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

import java.io.*;
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
    public void createAndSaveLeagueFrom(GameType kind, String leagueName, String divisionName) {
        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();

        if (leagueName.length() > 1
                && AppDatabase.getInstance(mContext).leagueDao().countByNameAndKind(leagueName, kind) == 0) {
            ApiLeague league = new ApiLeague();
            league.setId(UUID.randomUUID().toString());
            league.setCreatedBy(PrefUtils.getAuthentication(mContext).getUserId());
            league.setCreatedAt(utcTime);
            league.setUpdatedAt(utcTime);
            league.setKind(kind);
            league.setName(leagueName);
            league.setDivisions(new ArrayList<>());
            if (divisionName != null && divisionName.trim().length() > 1) {
                league.getDivisions().add(divisionName);
            }
            saveLeague(league);
        } else {
            ApiLeague league = getLeague(kind, leagueName);
            league.setUpdatedAt(utcTime);
            if (divisionName != null && divisionName.length() > 1 && !league.getDivisions().contains(divisionName)) {
                league.getDivisions().add(divisionName);
            }
            insertLeagueIntoDb(league, false, false);
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
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.LEAGUES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiLeagueDescription> leagueList = readLeagueList(response);
                        syncLeagues(leagueList, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_LEAGUES, String.format(Locale.getDefault(), "Error %d while synchronising league", error.networkResponse.statusCode));
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
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, String.format(ApiUtils.LEAGUE_API_URL, remoteLeague.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        ApiLeague league = readLeague(response);
                        insertLeagueIntoDb(league, true, false);
                        downloadLeaguesRecursive(remoteLeagues, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_LEAGUES, String.format(Locale.getDefault(), "Error %d while synchronising league", error.networkResponse.statusCode));
                        }
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void pushLeagueToServer(final ApiLeague league) {
        if (PrefUtils.canSync(mContext)) {
            final byte[] bytes = writeLeague(league).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, ApiUtils.LEAGUES_API_URL, bytes, PrefUtils.getAuthentication(mContext),
                    response -> insertLeagueIntoDb(league, true, false),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_LEAGUES, String.format(Locale.getDefault(), "Error %d while creating league", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }
}
