package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.api.ApiLeague;
import com.tonkar.volleyballreferee.api.ApiUtils;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.api.JsonStringRequest;
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
    public List<ApiLeague> listLeagues() {
        List<ApiLeague> leagues = new ArrayList<>();

        for (String content : AppDatabase.getInstance(mContext).leagueDao().listContents()) {
            leagues.add(readLeague(content));
        }

        return leagues;
    }

    @Override
    public List<String> listLeagueNames(GameType kind) {
        return AppDatabase.getInstance(mContext).leagueDao().listNamesByKind(kind.toString());
    }

    @Override
    public List<String> listDivisionNames(GameType kind, String leagueName) {
        ApiLeague league = getLeague(kind, leagueName);
        return league.getDivisions();
    }

    @Override
    public void createAndSaveLeagueFrom(GameType kind, String leagueName, String divisionName) {
        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();

        if (leagueName.trim().length() > 1 && AppDatabase.getInstance(mContext).leagueDao().countByNameAndKind(leagueName, kind.toString()) == 0) {
            ApiLeague league = getLeague(kind, leagueName);
            if (divisionName != null && divisionName.trim().length() > 1 && !league.getDivisions().contains(divisionName.trim())) {
                league.getDivisions().add(divisionName);
                league.setUpdatedAt(utcTime);
                insertLeagueIntoDb(league, false, false);
            }
        } else {
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
            insertLeagueIntoDb(league, false, false);
        }
    }

    private ApiLeague getLeague(GameType kind, String leagueName) {
        String json = AppDatabase.getInstance(mContext).leagueDao().findContentByNameAndKind(leagueName, kind.toString());
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

    private List<ApiLeague> readLeagueList(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.LEAGUE_LIST_TYPE);
    }

    private void insertLeagueIntoDb(final ApiLeague apiLeague, boolean synced, boolean syncInsertion) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
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
            }
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
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.LEAGUES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiLeague> leagueList = readLeagueList(response);
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

    private void syncLeagues(List<ApiLeague> remoteLeagueList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getAuthentication(mContext).getUserId();
        List<ApiLeague> localLeagueList = listLeagues();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiLeague localLeague : localLeagueList) {
            if (localLeague.getCreatedBy().equals(Authentication.VBR_USER_ID)) {
                localLeague.setCreatedBy(userId);
                localLeague.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                insertLeagueIntoDb(localLeague, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localLeagueList = listLeagues();
        }

        for (ApiLeague localLeague : localLeagueList) {
            boolean foundRemoteVersion = false;

            for (ApiLeague remoteLeague : remoteLeagueList) {
                if (localLeague.getId().equals(remoteLeague.getId())) {
                    foundRemoteVersion = true;
                    insertLeagueIntoDb(remoteLeague, true, true);
                }
            }

            if (!foundRemoteVersion) {
                if (localLeague.isSynced()) {
                    // if the league was synced, then it was deleted from the server and it must be deleted locally
                    deleteLeagueFromDb(localLeague.getId());
                } else {
                    // if the league was not synced, then it is missing from the server because sending it must have failed, so send it again
                    pushLeagueToServer(localLeague);
                }
            }
        }

        for (ApiLeague remoteLeague : remoteLeagueList) {
            boolean foundLocalVersion = false;

            for (ApiLeague localLeague : localLeagueList) {
                if (localLeague.getId().equals(remoteLeague.getId())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                insertLeagueIntoDb(remoteLeague, true, true);
            }
        }

        if (listener != null){
            listener.onSynchronizationSucceeded();
        }
    }

    private void pushLeagueToServer(final ApiLeague league) {
        if (PrefUtils.isSyncOn(mContext)) {
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
