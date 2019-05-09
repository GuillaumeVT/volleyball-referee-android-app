package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.TeamEntity;
import com.tonkar.volleyballreferee.business.team.BeachTeamDefinition;
import com.tonkar.volleyballreferee.business.team.EmptyTeamDefinition;
import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.data.StoredTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.*;

public class StoredTeams implements StoredTeamsService {

    private final Context mContext;

    public StoredTeams(Context context) {
        mContext = context;
    }

    @Override
    public List<ApiTeamDescription> listTeams() {
        return AppDatabase.getInstance(mContext).teamDao().listTeams();
    }

    @Override
    public List<ApiTeamDescription> listTeams(GameType kind) {
        return AppDatabase.getInstance(mContext).teamDao().listTeamsByKind(kind);
    }

    @Override
    public List<ApiTeamDescription> listTeams(GameType kind, GenderType genderType) {
        return AppDatabase.getInstance(mContext).teamDao().listTeamsByGenderAndKind(genderType, kind);
    }

    @Override
    public ApiTeam getTeam(String id) {
        String jsonTeam = AppDatabase.getInstance(mContext).teamDao().findContentById(id);
        return readTeam(jsonTeam);
    }

    @Override
    public ApiTeam getTeam(GameType kind, String teamName, GenderType genderType) {
        String jsonTeam = AppDatabase.getInstance(mContext).teamDao().findContentByNameAndGenderAndKind(teamName, genderType, kind);
        return readTeam(jsonTeam);
    }

    private WrappedTeam createWrappedTeam(GameType kind) {
        String id = UUID.randomUUID().toString();
        String userId = PrefUtils.getAuthentication(mContext).getUserId();

        TeamDefinition teamDefinition;

        switch (kind) {
            case INDOOR:
                teamDefinition = new IndoorTeamDefinition(kind, id, userId, TeamType.HOME);
                break;
            case INDOOR_4X4:
                teamDefinition = new IndoorTeamDefinition(kind, id, userId, TeamType.HOME);
                break;
            case BEACH:
                teamDefinition = new BeachTeamDefinition(id, userId, TeamType.HOME);
                break;
            case TIME:
            default:
                teamDefinition = new EmptyTeamDefinition(id, userId, TeamType.HOME);
                break;
        }

        return new WrappedTeam(teamDefinition);
    }

    @Override
    public BaseTeamService createTeam(GameType kind) {
        return createWrappedTeam(kind);
    }

    @Override
    public void saveTeam(BaseTeamService team) {
        ApiTeam savedTeam = copyTeam(team);
        savedTeam.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        insertTeamIntoDb(savedTeam, false, false);
        pushTeamToServer(savedTeam);
    }

    @Override
    public void deleteTeam(String id) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).teamDao().deleteById(id);
                deleteTeamOnServer(id);
            }
        }.start();
    }

    @Override
    public void deleteAllTeams() {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).teamDao().deleteAll();
                deleteAllTeamsOnServer();
            }
        }.start();
    }

    @Override
    public void createAndSaveTeamFrom(GameType kind, BaseTeamService teamService, TeamType teamType) {
        if (teamService.getTeamName(teamType).length() > 1
                && AppDatabase.getInstance(mContext).teamDao().countByNameAndGenderAndKind(teamService.getTeamName(teamType), teamService.getGender(teamType), kind) == 0
                && AppDatabase.getInstance(mContext).teamDao().countById(teamService.getTeamId(teamType)) == 0) {
            BaseTeamService team = createTeam(kind);
            copyTeam(teamService, team, teamType);
            saveTeam(team);
        }
    }

    @Override
    public ApiTeam copyTeam(BaseTeamService teamService) {
        ApiTeam team = new ApiTeam();
        copyTeam(teamService, team, TeamType.HOME);
        return team;
    }

    @Override
    public BaseTeamService copyTeam(ApiTeam team) {
        BaseTeamService teamService = createTeam(team.getKind());
        copyTeam(team, teamService, TeamType.HOME);
        return teamService;
    }

    @Override
    public void copyTeam(ApiTeam source, BaseTeamService dest, TeamType teamType) {
        dest.setTeamId(teamType, source.getId());
        dest.setCreatedBy(teamType, source.getCreatedBy());
        dest.setCreatedAt(teamType, source.getCreatedAt());
        dest.setUpdatedAt(teamType, source.getUpdatedAt());
        dest.setTeamName(teamType, source.getName());
        dest.setTeamColor(teamType, source.getColorInt());
        dest.setLiberoColor(teamType, source.getLiberoColorInt());
        dest.setGender(teamType, source.getGender());

        for (ApiPlayer player : dest.getPlayers(teamType)) {
            dest.removePlayer(teamType, player.getNum());
        }
        for (ApiPlayer player : dest.getLiberos(teamType)) {
            dest.removeLibero(teamType, player.getNum());
        }
        for (ApiPlayer player : source.getPlayers()) {
            dest.addPlayer(teamType, player.getNum());
        }
        for (ApiPlayer player : source.getLiberos()) {
            dest.addLibero(teamType, player.getNum());
        }

        dest.setCaptain(teamType, source.getCaptain());
    }

    @Override
    public void copyTeam(BaseTeamService source, ApiTeam dest, TeamType teamType) {
        dest.setId(source.getTeamId(teamType));
        dest.setCreatedBy(source.getCreatedBy(teamType));
        dest.setCreatedAt(source.getCreatedAt(teamType));
        dest.setUpdatedAt(source.getUpdatedAt(teamType));
        dest.setKind(source.getTeamsKind());
        dest.setName(source.getTeamName(teamType));
        dest.setColorInt(source.getTeamColor(teamType));
        dest.setLiberoColorInt(source.getLiberoColor(teamType));
        dest.setGender(source.getGender(teamType));

        dest.getPlayers().clear();
        dest.getLiberos().clear();

        for (ApiPlayer player : source.getPlayers(teamType)) {
            dest.getPlayers().add(player);
        }
        for (ApiPlayer player : source.getLiberos(teamType)) {
            dest.getLiberos().add(player);
        }

        dest.setCaptain(source.getCaptain(teamType));
    }

    private void copyTeam(BaseTeamService source, BaseTeamService dest, TeamType teamType) {
        dest.setTeamId(teamType, source.getTeamId(teamType));
        dest.setCreatedBy(teamType, source.getCreatedBy(teamType));
        dest.setCreatedAt(teamType, source.getCreatedAt(teamType));
        dest.setUpdatedAt(teamType, source.getUpdatedAt(teamType));
        dest.setTeamName(teamType, source.getTeamName(teamType));
        dest.setTeamColor(teamType, source.getTeamColor(teamType));
        dest.setLiberoColor(teamType, source.getLiberoColor(teamType));
        dest.setGender(teamType, source.getGender(teamType));

        for (ApiPlayer player : dest.getPlayers(teamType)) {
            dest.removePlayer(teamType, player.getNum());
        }
        for (ApiPlayer player : dest.getLiberos(teamType)) {
            dest.removeLibero(teamType, player.getNum());
        }

        for (ApiPlayer player : source.getPlayers(teamType)) {
            dest.addPlayer(teamType, player.getNum());
        }
        for (ApiPlayer player : source.getLiberos(teamType)) {
            dest.addLibero(teamType, player.getNum());
        }

        dest.setCaptain(teamType, source.getCaptain(teamType));
    }

    // Read saved teams

    @Override
    public boolean hasTeams() {
        return AppDatabase.getInstance(mContext).teamDao().count() > 0;
    }

    public static List<ApiTeam> readTeamsStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.TEAM_LIST_TYPE);
        }
    }

    @Override
    public ApiTeam readTeam(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.TEAM_TYPE);
    }

    private List<ApiTeamDescription> readTeams(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.TEAM_DESCRIPTION_LIST_TYPE);
    }

    // Write saved teams

    private void insertTeamIntoDb(final ApiTeam apiTeam, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            String json = writeTeam(apiTeam);
            TeamEntity teamEntity = new TeamEntity();
            teamEntity.setId(apiTeam.getId());
            teamEntity.setCreatedBy(apiTeam.getCreatedBy());
            teamEntity.setCreatedAt(apiTeam.getCreatedAt());
            teamEntity.setUpdatedAt(apiTeam.getUpdatedAt());
            teamEntity.setKind(apiTeam.getKind());
            teamEntity.setGender(apiTeam.getGender());
            teamEntity.setName(apiTeam.getName());
            teamEntity.setSynced(synced);
            teamEntity.setContent(json);
            AppDatabase.getInstance(mContext).teamDao().insert(teamEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            new Thread(runnable).start();
        }
    }

    public static void writeTeamsStream(OutputStream outputStream, List<ApiTeam> teams) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(teams, JsonIOUtils.TEAM_LIST_TYPE, writer);
        writer.close();
    }

    @Override
    public String writeTeam(ApiTeam team) {
        return JsonIOUtils.GSON.toJson(team, JsonIOUtils.TEAM_TYPE);
    }

    // Web

    @Override
    public void syncTeams() {
        syncTeams(null);
    }

    @Override
    public void syncTeams(final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.TEAMS_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiTeamDescription> teamList = readTeams(response);
                        syncTeams(teamList, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", error.networkResponse.statusCode));
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

    private void syncTeams(List<ApiTeamDescription> remoteTeamList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getAuthentication(mContext).getUserId();
        List<ApiTeamDescription> localTeamList = listTeams();
        Queue<ApiTeamDescription> remoteTeamsToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiTeamDescription localTeam : localTeamList) {
            if (localTeam.getCreatedBy().equals(Authentication.VBR_USER_ID)) {
                ApiTeam team = getTeam(localTeam.getId());
                team.setCreatedBy(userId);
                team.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                insertTeamIntoDb(team, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localTeamList = listTeams();
        }

        for (ApiTeamDescription localTeam : localTeamList) {
            boolean foundRemoteVersion = false;

            for (ApiTeamDescription remoteTeam : remoteTeamList) {
                if (localTeam.getId().equals(remoteTeam.getId())) {
                    foundRemoteVersion = true;

                    if (localTeam.getUpdatedAt() < remoteTeam.getUpdatedAt()) {
                        remoteTeamsToDownload.add(remoteTeam);
                    } else if (localTeam.getUpdatedAt() > remoteTeam.getUpdatedAt()) {
                        ApiTeam team = getTeam(localTeam.getId());
                        pushTeamToServer(team);
                    }
                }
            }

            if (!foundRemoteVersion) {
                if (localTeam.isSynced()) {
                    // if the team was synced, then it was deleted from the server and it must be deleted locally
                    deleteTeam(localTeam.getId());
                } else {
                    // if the team was not synced, then it is missing from the server because sending it must have failed, so send it again
                    ApiTeam team = getTeam(localTeam.getId());
                    pushTeamToServer(team);
                }
            }
        }

        for (ApiTeamDescription remoteTeam : remoteTeamList) {
            boolean foundLocalVersion = false;

            for (ApiTeamDescription localTeam : localTeamList) {
                if (localTeam.getId().equals(remoteTeam.getId())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                remoteTeamsToDownload.add(remoteTeam);
            }
        }

        downloadTeamsRecursive(remoteTeamsToDownload, listener);
    }

    private void downloadTeamsRecursive(final Queue<ApiTeamDescription> remoteTeams, final DataSynchronizationListener listener) {
        if (remoteTeams.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiTeamDescription remoteTeam = remoteTeams.poll();
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, String.format(ApiUtils.TEAM_API_URL, remoteTeam.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        ApiTeam team = readTeam(response);
                        insertTeamIntoDb(team, true, false);
                        downloadTeamsRecursive(remoteTeams, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", error.networkResponse.statusCode));
                        }
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void pushTeamToServer(final ApiTeam team) {
        if (PrefUtils.canSync(mContext)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            final byte[] bytes = writeTeam(team).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, ApiUtils.TEAMS_API_URL, bytes, authentication,
                    response -> insertTeamIntoDb(team, true, false),
                    error -> {
                        if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                            JsonStringRequest stringRequest1 = new JsonStringRequest(Request.Method.POST, ApiUtils.TEAMS_API_URL, bytes, authentication,
                                    response -> insertTeamIntoDb(team, true, false),
                                    error2 -> {
                                        if (error2.networkResponse != null) {
                                            Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while creating team", error2.networkResponse.statusCode));
                                        }
                                    }
                            );
                            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest1);
                        } else {
                            if (error.networkResponse != null) {
                                Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while creating team", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteTeamOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, String.format(ApiUtils.TEAM_API_URL, id), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while deleting team", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllTeamsOnServer() {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, ApiUtils.TEAMS_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while deleting all teams", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }
}
