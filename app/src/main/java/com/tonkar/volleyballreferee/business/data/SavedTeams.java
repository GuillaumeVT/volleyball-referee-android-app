package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private final Context     mContext;
    private       WrappedTeam mCurrentTeam;

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
    public void createTeam(GameType gameType) {
        mCurrentTeam = createWrappedTeam(gameType);
    }

    @Override
    public void editTeam(GameType gameType, String teamName, GenderType genderType) {
        RecordedTeam savedTeam = getSavedTeam(gameType, teamName, genderType);
        mCurrentTeam = createWrappedTeam(gameType);
        copyTeam(savedTeam, mCurrentTeam, TeamType.HOME);
    }

    @Override
    public BaseTeamService getCurrentTeam() {
        return mCurrentTeam;
    }

    @Override
    public void saveCurrentTeam() {
        RecordedTeam savedTeam = new RecordedTeam();
        copyTeam(mCurrentTeam, savedTeam, TeamType.HOME);
        insertTeamIntoDb(savedTeam);
        pushTeamOnline(savedTeam);
        mCurrentTeam = null;
    }

    @Override
    public void cancelCurrentTeam() {
        mCurrentTeam = null;
    }

    @Override
    public void deleteSavedTeam(final GameType gameType, final String teamName, final GenderType genderType) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).teamDao().deleteByNameAndGenderAndKind(teamName, genderType.toString(), gameType.toString());
                deleteTeamOnline(gameType, teamName, genderType);
                mCurrentTeam = null;
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
        if (AppDatabase.getInstance(mContext).teamDao().countByNameAndGenderAndKind(teamService.getTeamName(teamType), teamService.getGenderType(teamType).toString(), gameType.toString()) == 0) {
            createTeam(gameType);
            copyTeam(teamService, mCurrentTeam, teamType);
            saveCurrentTeam();
        }
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
    public void migrateSavedTeams() {
        String filename = "device_saved_teams.json";
        File teamsFile = mContext.getFileStreamPath(filename);

        if (teamsFile != null && teamsFile.exists()) {
            Log.i(Tags.SAVED_TEAMS, String.format("Migrate saved teams from %s", filename));

            try {
                FileInputStream inputStream = mContext.openFileInput(filename);
                List<RecordedTeam> teams = readTeamsStream(inputStream);
                inputStream.close();

                final List<TeamEntity> teamEntities = new ArrayList<>();

                for (RecordedTeam team : teams) {
                    teamEntities.add(new TeamEntity(team.getName(), team.getGenderType().toString(), team.getGameType().toString(), writeTeam(team)));
                }

                new Thread() {
                    public void run() {
                        AppDatabase.getInstance(mContext).teamDao().insertAll(teamEntities);
                        syncTeamsOnline();
                    }
                }.start();

                mContext.deleteFile(filename);
            } catch (FileNotFoundException e) {
                Log.i(Tags.SAVED_TEAMS, String.format("%s saved teams file does not exist", filename));
            } catch (JsonParseException | IOException e) {
                Log.e(Tags.SAVED_TEAMS, "Exception while reading teams", e);
            }
        }
    }

    @Override
    public boolean hasSavedTeams() {
        return AppDatabase.getInstance(mContext).teamDao().count() > 0;
    }

    public static List<RecordedTeam> readTeamsStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RECORDED_TEAM_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    private RecordedTeam readTeam(String json) {
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

    private String writeTeam(RecordedTeam team) {
        return JsonIOUtils.GSON.toJson(team, JsonIOUtils.RECORDED_TEAM_TYPE);
    }

    // Web

    private void syncTeams(List<RecordedTeam> remoteTeamList) {
        List<RecordedTeam> localTeamList = getSavedTeamList();

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
                pushTeamOnline(localTeam);
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
                insertTeamIntoDb(remoteTeam);
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
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            List<RecordedTeam> teamList = readTeams(response);
                            syncTeams(teamList);
                            if (listener != null){
                                listener.onSynchronizationSucceeded();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", error.networkResponse.statusCode));
                            }
                            if (listener != null){
                                listener.onSynchronizationFailed();
                            }
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

    private void pushTeamOnline(RecordedTeam team) {
        if (PrefUtils.isSyncOn(mContext)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            team.setUserId(authentication.getUserId());
            final byte[] bytes = writeTeam(team).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, WebUtils.USER_TEAM_API_URL, bytes, authentication,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.POST, WebUtils.USER_TEAM_API_URL, bytes, authentication,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {}
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                if (error.networkResponse != null) {
                                                    Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while creating team", error.networkResponse.statusCode));
                                                }
                                            }
                                        }
                                );
                                WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
                            } else {
                                if (error.networkResponse != null) {
                                    Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while creating team", error.networkResponse.statusCode));
                                }
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteTeamOnline(GameType gameType, String teamName, GenderType genderType) {
        if (PrefUtils.isSyncOn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("name", teamName);
            params.put("gender", genderType.toString());
            params.put("kind", gameType.toString());
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_TEAM_API_URL + parameters, new byte[0], PrefUtils.getAuthentication(mContext),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while deleting team", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllTeamsOnline() {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_TEAM_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(Tags.SAVED_TEAMS, String.format(Locale.getDefault(), "Error %d while deleting all teams", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }
}
