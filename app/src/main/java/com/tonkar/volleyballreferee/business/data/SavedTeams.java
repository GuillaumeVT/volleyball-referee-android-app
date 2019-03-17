package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.SyncEntity;
import com.tonkar.volleyballreferee.business.data.db.TeamEntity;
import com.tonkar.volleyballreferee.business.team.BeachTeamDefinition;
import com.tonkar.volleyballreferee.business.team.EmptyTeamDefinition;
import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.business.web.Authentication;
import com.tonkar.volleyballreferee.business.web.JsonStringRequest;
import com.tonkar.volleyballreferee.business.web.WebUtils;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.data.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SavedTeams implements SavedTeamsService {

    private final Context mContext;

    public SavedTeams(Context context) {
        mContext = context;
    }

    @Override
    public List<RecordedTeam> getSavedTeamList() {
        List<String> jsonTeamList = AppDatabase.getInstance(mContext).teamDao().getAllContents();
        List<RecordedTeam> teams = new ArrayList<>();

        for (String jsonTeam : jsonTeamList) {
            teams.add(readTeam(jsonTeam));
        }

        return teams;
    }

    @Override
    public List<RecordedTeam> getSavedTeamList(GameType gameType) {
        List<String> jsonTeamList = AppDatabase.getInstance(mContext).teamDao().findContentByKind(gameType.toString());
        List<RecordedTeam> teams = new ArrayList<>();

        for (String jsonTeam : jsonTeamList) {
            teams.add(readTeam(jsonTeam));
        }

        return teams;
    }

    @Override
    public List<String> getSavedTeamNameList(GameType gameType, GenderType genderType) {
        return AppDatabase.getInstance(mContext).teamDao().findNamesByGenderAndKind(genderType.toString(), gameType.toString());
    }

    @Override
    public RecordedTeam getSavedTeam(GameType gameType, String teamName, GenderType genderType) {
        String jsonTeam = AppDatabase.getInstance(mContext).teamDao().findContentByNameAndGenderAndKind(teamName, genderType.toString(), gameType.toString());
        return readTeam(jsonTeam);
    }

    private WrappedTeam createWrappedTeam(GameType gameType) {
        TeamDefinition teamDefinition;

        switch (gameType) {
            case INDOOR:
                teamDefinition = new IndoorTeamDefinition(gameType, TeamType.HOME);
                break;
            case INDOOR_4X4:
                teamDefinition = new IndoorTeamDefinition(gameType, TeamType.HOME);
                break;
            case BEACH:
                teamDefinition = new BeachTeamDefinition(TeamType.HOME);
                break;
            case TIME:
            default:
                teamDefinition = new EmptyTeamDefinition(TeamType.HOME);
                break;
        }

        return new WrappedTeam(teamDefinition);
    }

    @Override
    public BaseTeamService createTeam(GameType gameType) {
        return createWrappedTeam(gameType);
    }

    @Override
    public void saveTeam(BaseTeamService team) {
        RecordedTeam savedTeam = copyTeam(team);
        insertTeamIntoDb(savedTeam);
        pushTeamOnline(savedTeam);
    }

    @Override
    public void deleteSavedTeam(final GameType gameType, final String teamName, final GenderType genderType) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).teamDao().deleteByNameAndGenderAndKind(teamName, genderType.toString(), gameType.toString());
                deleteTeamOnline(gameType, teamName, genderType);
            }
        }.start();
    }

    @Override
    public void deleteAllSavedTeams() {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).teamDao().deleteAll();
                deleteAllTeamsOnline();
            }
        }.start();
    }

    @Override
    public void createAndSaveTeamFrom(GameType gameType, BaseTeamService teamService, TeamType teamType) {
        if (teamService.getTeamName(teamType).length() > 1
                && AppDatabase.getInstance(mContext).teamDao().countByNameAndGenderAndKind(teamService.getTeamName(teamType), teamService.getGenderType(teamType).toString(), gameType.toString()) == 0) {
            BaseTeamService team = createTeam(gameType);
            copyTeam(teamService, team, teamType);
            saveTeam(team);
        }
    }

    @Override
    public RecordedTeam copyTeam(BaseTeamService teamService) {
        RecordedTeam team = new RecordedTeam();
        copyTeam(teamService, team, TeamType.HOME);
        return team;
    }

    @Override
    public BaseTeamService copyTeam(RecordedTeam team) {
        BaseTeamService teamService = createTeam(team.getGameType());
        copyTeam(team, teamService, TeamType.HOME);
        return teamService;
    }

    @Override
    public void copyTeam(RecordedTeam source, BaseTeamService dest, TeamType teamType) {
        dest.setTeamName(teamType, source.getName());
        dest.setTeamColor(teamType, source.getColor());
        dest.setLiberoColor(teamType, source.getLiberoColor());
        dest.setGenderType(teamType, source.getGenderType());

        for (Integer number: dest.getPlayers(teamType)) {
            dest.removePlayer(teamType, number);
        }
        for (Integer number: dest.getLiberos(teamType)) {
            dest.removeLibero(teamType, number);
        }

        for (Integer number: source.getPlayers()) {
            dest.addPlayer(teamType, number);
        }
        for (Integer number: source.getLiberos()) {
            dest.addLibero(teamType, number);
        }

        dest.setCaptain(teamType, source.getCaptain());
    }

    @Override
    public void copyTeam(BaseTeamService source, RecordedTeam dest, TeamType teamType) {
        dest.setDate(System.currentTimeMillis());
        dest.setGameType(source.getTeamsKind());
        dest.setName(source.getTeamName(teamType));
        dest.setColor(source.getTeamColor(teamType));
        dest.setLiberoColor(source.getLiberoColor(teamType));
        dest.setGenderType(source.getGenderType());

        dest.getPlayers().clear();
        dest.getLiberos().clear();

        for (Integer number: source.getPlayers(teamType)) {
            dest.getPlayers().add(number);
        }
        for (Integer number: source.getLiberos(teamType)) {
            dest.getLiberos().add(number);
        }

        dest.setCaptain(source.getCaptain(teamType));
    }

    private void copyTeam(BaseTeamService source, BaseTeamService dest, TeamType teamType) {
        dest.setTeamName(teamType, source.getTeamName(teamType));
        dest.setTeamColor(teamType, source.getTeamColor(teamType));
        dest.setLiberoColor(teamType, source.getLiberoColor(teamType));
        dest.setGenderType(teamType, source.getGenderType(teamType));

        for (Integer number: dest.getPlayers(teamType)) {
            dest.removePlayer(teamType, number);
        }
        for (Integer number: dest.getLiberos(teamType)) {
            dest.removeLibero(teamType, number);
        }

        for (Integer number: source.getPlayers(teamType)) {
            dest.addPlayer(teamType, number);
        }
        for (Integer number: source.getLiberos(teamType)) {
            dest.addLibero(teamType, number);
        }

        dest.setCaptain(teamType, source.getCaptain(teamType));
    }

    // Read saved teams

    @Override
    public boolean hasSavedTeams() {
        return AppDatabase.getInstance(mContext).teamDao().count() > 0;
    }

    public static List<RecordedTeam> readTeamsStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RECORDED_TEAM_LIST_TYPE);
        }
    }

    @Override
    public RecordedTeam readTeam(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RECORDED_TEAM_TYPE);
    }

    private List<RecordedTeam> readTeams(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RECORDED_TEAM_LIST_TYPE);
    }

    // Write saved teams

    private void insertTeamIntoDb(final RecordedTeam recordedTeam) {
        new Thread() {
            public void run() {
                String json = writeTeam(recordedTeam);
                TeamEntity teamEntity = new TeamEntity(recordedTeam.getName(), recordedTeam.getGenderType().toString(), recordedTeam.getGameType().toString(), json);
                AppDatabase.getInstance(mContext).teamDao().insert(teamEntity);
            }
        }.start();
    }

    public static void writeTeamsStream(OutputStream outputStream, List<RecordedTeam> teams) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(teams, JsonIOUtils.RECORDED_TEAM_LIST_TYPE, writer);
        writer.close();
    }

    @Override
    public String writeTeam(RecordedTeam team) {
        return JsonIOUtils.GSON.toJson(team, JsonIOUtils.RECORDED_TEAM_TYPE);
    }

    // Web

    private void syncTeams(List<RecordedTeam> remoteTeamList) {
        String userId = PrefUtils.getAuthentication(mContext).getUserId();
        List<RecordedTeam> localTeamList = getSavedTeamList();

        for (RecordedTeam localTeam : localTeamList) {
            if (localTeam.getUserId().equals(Authentication.VBR_USER_ID)) {
                localTeam.setUserId(userId);
                insertTeamIntoDb(localTeam);
            }
        }

        for (RecordedTeam localTeam : localTeamList) {
            boolean foundRemoteVersion = false;

            for (RecordedTeam remoteTeam : remoteTeamList) {
                if (localTeam.getName().equals(remoteTeam.getName()) && localTeam.getGenderType().equals(remoteTeam.getGenderType()) && localTeam.getGameType().equals(remoteTeam.getGameType())) {
                    foundRemoteVersion = true;

                    if (localTeam.getDate() < remoteTeam.getDate()) {
                        localTeam.setAll(remoteTeam);
                        insertTeamIntoDb(localTeam);
                    } else if (localTeam.getDate() > remoteTeam.getDate()) {
                        pushTeamOnline(localTeam);
                    }
                }
            }

            if (!foundRemoteVersion) {
                if (isSynced(localTeam)) {
                    // if the team was synced, then it was deleted from the server and it must be deleted locally
                    deleteSavedTeam(localTeam.getGameType(), localTeam.getName(), localTeam.getGenderType());
                } else {
                    // if the team was not synced, then it is missing from the server because sending it must have failed, so send it again
                    pushTeamOnline(localTeam);
                }
            }
        }

        for (RecordedTeam remoteTeam : remoteTeamList) {
            boolean foundLocalVersion = false;

            for (RecordedTeam localTeam : localTeamList) {
                if (localTeam.getName().equals(remoteTeam.getName()) && localTeam.getGenderType().equals(remoteTeam.getGenderType()) && localTeam.getGameType().equals(remoteTeam.getGameType())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                if (isSynced(remoteTeam)) {
                    // if the team was synced, then sending the deletion to the server must have failed, so send the deletion again
                    deleteTeamOnline(remoteTeam.getGameType(), remoteTeam.getName(), remoteTeam.getGenderType());
                } else {
                    // if the team was not synced, then it was added on the server and it must be added locally
                    insertTeamIntoDb(remoteTeam);
                    pushTeamOnline(remoteTeam);
                }
            }
        }
    }

    @Override
    public void syncTeamsOnline() {
        syncTeamsOnline(null);
    }

    @Override
    public void syncTeamsOnline(final DataSynchronizationListener listener) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_TEAM_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<RecordedTeam> teamList = readTeams(response);
                        syncTeams(teamList);
                        if (listener != null){
                            listener.onSynchronizationSucceeded();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", error.networkResponse.statusCode));
                        }
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            if (listener != null){
                listener.onSynchronizationFailed();
            }
        }
    }

    private void pushTeamOnline(final RecordedTeam team) {
        if (PrefUtils.isSyncOn(mContext)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            team.setUserId(authentication.getUserId());
            final byte[] bytes = writeTeam(team).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, WebUtils.USER_TEAM_API_URL, bytes, authentication,
                    response -> insertSyncIntoDb(team),
                    error -> {
                        if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                            JsonStringRequest stringRequest1 = new JsonStringRequest(Request.Method.POST, WebUtils.USER_TEAM_API_URL, bytes, authentication,
                                    response -> insertSyncIntoDb(team),
                                    error2 -> {
                                        if (error2.networkResponse != null) {
                                            Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while creating team", error2.networkResponse.statusCode));
                                        }
                                    }
                            );
                            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest1);
                        } else {
                            if (error.networkResponse != null) {
                                Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while creating team", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteTeamOnline(final GameType gameType, final String teamName, final GenderType genderType) {
        if (PrefUtils.isSyncOn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("name", teamName);
            params.put("gender", genderType.toString());
            params.put("kind", gameType.toString());
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_TEAM_API_URL + parameters, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> AppDatabase.getInstance(mContext).syncDao().deleteByItemAndType(SyncEntity.createTeamItem(teamName, genderType.toString(), gameType.toString()), SyncEntity.RULES_ENTITY),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while deleting team", error.networkResponse.statusCode));
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllTeamsOnline() {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_TEAM_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> AppDatabase.getInstance(mContext).syncDao().deleteByType(SyncEntity.TEAM_ENTITY),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while deleting all teams", error.networkResponse.statusCode));
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private boolean isSynced(RecordedTeam recordedTeam) {
        return AppDatabase.getInstance(mContext).syncDao().countByItemAndType(SyncEntity.createTeamItem(recordedTeam.getName(), recordedTeam.getGenderType().toString(), recordedTeam.getGameType().toString()), SyncEntity.TEAM_ENTITY) > 0;
    }

    private void insertSyncIntoDb(RecordedTeam recordedTeam) {
        AppDatabase.getInstance(mContext).syncDao().insert(SyncEntity.createTeamSyncEntity(recordedTeam.getName(), recordedTeam.getGenderType().toString(), recordedTeam.getGameType().toString()));
    }
}
