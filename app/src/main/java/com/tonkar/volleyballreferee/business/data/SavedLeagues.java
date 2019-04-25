package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import com.tonkar.volleyballreferee.api.ApiLeague;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.LeagueEntity;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.SavedLeaguesService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SavedLeagues implements SavedLeaguesService {

    private final Context mContext;

    public SavedLeagues(Context context) {
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
        if (leagueName.trim().length() > 1 && AppDatabase.getInstance(mContext).leagueDao().countByNameAndKind(leagueName, kind.toString()) == 0) {
            ApiLeague league = getLeague(kind, leagueName);
            if (divisionName != null && divisionName.trim().length() > 1 && !league.getDivisions().contains(divisionName.trim())) {
                league.getDivisions().add(divisionName);
                league.setUpdatedAt(System.currentTimeMillis());
                insertLeagueIntoDb(league, false);
            }
        } else {
            ApiLeague league = new ApiLeague();
            league.setId(UUID.randomUUID().toString());
            league.setCreatedBy(PrefUtils.getAuthentication(mContext).getUserId());
            league.setCreatedAt(System.currentTimeMillis());
            league.setUpdatedAt(System.currentTimeMillis());
            league.setKind(kind);
            league.setName(leagueName);
            league.setDivisions(new ArrayList<>());
            if (divisionName != null && divisionName.trim().length() > 1) {
                league.getDivisions().add(divisionName);
            }
            insertLeagueIntoDb(league, false);
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

    private void insertLeagueIntoDb(final ApiLeague apiLeague, boolean synced) {
        new Thread() {
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
        }.start();
    }

    @Override
    public void syncLeagues() {

    }

    @Override
    public void syncLeagues(DataSynchronizationListener listener) {

    }
}
