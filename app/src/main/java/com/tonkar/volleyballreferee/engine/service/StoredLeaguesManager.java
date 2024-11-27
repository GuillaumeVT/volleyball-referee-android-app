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

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

import okhttp3.*;

public class StoredLeaguesManager implements StoredLeaguesService {

    private final Context       mContext;
    private final VbrRepository mRepository;

    public StoredLeaguesManager(Context context) {
        mContext = context;
        mRepository = new VbrRepository(mContext);
    }

    @Override
    public List<LeagueSummaryDto> listLeagues() {
        return mRepository.listLeagues();
    }

    @Override
    public List<LeagueSummaryDto> listLeagues(GameType kind) {
        return mRepository.listLeagues(kind);
    }

    @Override
    public List<String> listDivisionNames(String id) {
        LeagueDto league = mRepository.getLeague(id);
        return league.getDivisions();
    }

    private void saveLeague(LeagueDto league) {
        league.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        mRepository.insertLeague(league, false, false);
        pushLeagueToServer(league);
    }

    @Override
    public void createAndSaveLeagueFrom(SelectedLeagueDto selectedLeague) {
        if (selectedLeague.getName().length() > 1 && selectedLeague.getDivision().length() > 1) {
            long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();

            int leaguesSameName = mRepository.countLeagues(selectedLeague.getName(), selectedLeague.getKind());
            int leaguesSameId = mRepository.countLeagues(selectedLeague.getId());

            if (leaguesSameName == 0 && leaguesSameId == 0) {
                LeagueDto league = new LeagueDto();
                league.setAll(selectedLeague);
                saveLeague(league);
            } else if (leaguesSameName == 1 && leaguesSameId == 1) {
                LeagueDto league = mRepository.getLeague(selectedLeague.getId());
                if (!league.getDivisions().contains(selectedLeague.getDivision())) {
                    league.setUpdatedAt(utcTime);
                    league.getDivisions().add(selectedLeague.getDivision());
                    mRepository.insertLeague(league, false, false);
                }
            }
        }
    }

    @Override
    public LeagueDto getLeague(GameType kind, String leagueName) {
        return mRepository.getLeague(leagueName, kind);
    }

    public static List<LeagueDto> readLeaguesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return JsonConverters.GSON.fromJson(reader, new TypeToken<List<LeagueDto>>() {}.getType());
        }
    }

    public static void writeLeaguesStream(OutputStream outputStream, List<LeagueDto> leagues) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        JsonConverters.GSON.toJson(leagues, new TypeToken<List<LeagueDto>>() {}.getType(), writer);
        writer.close();
    }

    @Override
    public void syncLeagues() {
        syncLeagues(null);
    }

    @Override
    public void syncLeagues(DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).getLeagueList(mContext, new Callback() {
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
                            List<LeagueSummaryDto> leagueList = JsonConverters.GSON.fromJson(body.string(),
                                                                                             new TypeToken<List<LeagueSummaryDto>>() {}.getType());
                            syncLeagues(leagueList, listener);
                        }
                    } else {
                        Log.e(Tags.STORED_LEAGUES,
                              String.format(Locale.getDefault(), "Error %d while synchronising leagues", response.code()));
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

    private void syncLeagues(List<LeagueSummaryDto> remoteLeagueList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getUserId(mContext);
        List<LeagueSummaryDto> localLeagueList = listLeagues();
        Queue<LeagueSummaryDto> remoteLeaguesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (LeagueSummaryDto localLeague : localLeagueList) {
            if (StringUtils.isBlank(localLeague.getCreatedBy())) {
                LeagueDto league = mRepository.getLeague(localLeague.getId());
                league.setCreatedBy(userId);
                league.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                mRepository.insertLeague(league, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localLeagueList = listLeagues();
        }

        for (LeagueSummaryDto localLeague : localLeagueList) {
            boolean foundRemoteVersion = false;

            for (LeagueSummaryDto remoteLeague : remoteLeagueList) {
                if (localLeague.getId().equals(remoteLeague.getId())) {
                    foundRemoteVersion = true;
                    remoteLeaguesToDownload.add(remoteLeague);
                }
            }

            if (!foundRemoteVersion) {
                if (localLeague.isSynced()) {
                    // if the league was synced, then it was deleted from the server and it must be deleted locally
                    mRepository.deleteLeague(localLeague.getId());
                } else {
                    // if the league was not synced, then it is missing from the server because sending it must have failed, so send it again
                    LeagueDto league = mRepository.getLeague(localLeague.getId());
                    pushLeagueToServer(league);
                }
            }
        }

        for (LeagueSummaryDto remoteLeague : remoteLeagueList) {
            boolean foundLocalVersion = false;

            for (LeagueSummaryDto localLeague : localLeagueList) {
                if (localLeague.getId().equals(remoteLeague.getId())) {
                    foundLocalVersion = true;
                    break;
                }
            }

            if (!foundLocalVersion) {
                remoteLeaguesToDownload.add(remoteLeague);
            }
        }

        downloadLeaguesRecursive(remoteLeaguesToDownload, listener);
    }

    private void downloadLeaguesRecursive(final Queue<LeagueSummaryDto> remoteLeagues, final DataSynchronizationListener listener) {
        if (remoteLeagues.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            LeagueSummaryDto remoteLeague = remoteLeagues.poll();
            VbrApi.getInstance(mContext).getLeague(remoteLeague.getId(), mContext, new Callback() {
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
                            LeagueDto league = JsonConverters.GSON.fromJson(body.string(), LeagueDto.class);
                            mRepository.insertLeague(league, true, false);
                            downloadLeaguesRecursive(remoteLeagues, listener);
                        }
                    } else {
                        Log.e(Tags.STORED_LEAGUES,
                              String.format(Locale.getDefault(), "Error %d while synchronising league", response.code()));
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }

    private void pushLeagueToServer(final LeagueDto league) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).createLeague(league, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        mRepository.insertLeague(league, true, false);
                    } else {
                        Log.e(Tags.STORED_LEAGUES, String.format(Locale.getDefault(), "Error %d while posting league", response.code()));
                    }
                }
            });
        }
    }
}
