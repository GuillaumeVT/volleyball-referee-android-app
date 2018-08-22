package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.PrefUtils;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private final Context            mContext;
    private final List<RecordedTeam> mSavedTeams;
    private       WrappedTeam        mWrappedTeam;

    public SavedTeams(Context context) {
        mContext = context;
        mSavedTeams = new ArrayList<>();
    }

    @Override
    public void loadSavedTeams() {
        mWrappedTeam = null;
        readSavedTeams();
        syncTeamsOnline();
    }

    @Override
    public List<RecordedTeam> getSavedTeamList() {
        return new ArrayList<>(mSavedTeams);
    }

    @Override
    public List<RecordedTeam> getSavedTeamList(GameType gameType) {
        List<RecordedTeam> list = new ArrayList<>();

        for (RecordedTeam team: mSavedTeams) {
            if (gameType.equals(team.getGameType())) {
                list.add(team);
            }
        }

        return list;
    }

    @Override
    public List<String> getSavedTeamNameList(GameType gameType, GenderType genderType) {
        List<String> list = new ArrayList<>();

        for (RecordedTeam team: mSavedTeams) {
            if (gameType.equals(team.getGameType()) && genderType.equals(team.getGenderType())) {
                list.add(team.getName());
            }
        }

        return list;
    }

    @Override
    public RecordedTeam getSavedTeam(GameType gameType, String teamName, GenderType genderType) {
        RecordedTeam matching = null;

        for (RecordedTeam team : mSavedTeams) {
            if (team.getGameType().equals(gameType)
                    && team.getName().equals(teamName)
                    && team.getGenderType().equals(genderType)) {
                matching = team;
            }
        }

        return matching;
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
        mWrappedTeam = createWrappedTeam(gameType);
    }

    @Override
    public void editTeam(GameType gameType, String teamName, GenderType genderType) {
        RecordedTeam savedTeam = getSavedTeam(gameType, teamName, genderType);
        mWrappedTeam = createWrappedTeam(gameType);
        copyTeam(savedTeam, mWrappedTeam, TeamType.HOME);
        mSavedTeams.remove(savedTeam);
    }

    @Override
    public BaseTeamService getCurrentTeam() {
        return mWrappedTeam;
    }

    @Override
    public synchronized void saveCurrentTeam() {
        RecordedTeam savedTeam = new RecordedTeam();
        copyTeam(mWrappedTeam, savedTeam, TeamType.HOME);
        mSavedTeams.add(savedTeam);
        writeSavedTeams();
        pushTeamOnline(savedTeam);
        mWrappedTeam = null;
    }

    @Override
    public void deleteSavedTeam(GameType gameType, String teamName, GenderType genderType) {
        if (mWrappedTeam != null && mWrappedTeam.getTeamsKind().equals(gameType)
                && mWrappedTeam.getTeamName(null).equals(teamName)
                && mWrappedTeam.getGenderType().equals(genderType)) {
            deleteTeamOnline(gameType, teamName, genderType);
            writeSavedTeams();
            mWrappedTeam = null;
        }
    }

    @Override
    public void deleteAllSavedTeams() {
        mSavedTeams.clear();
        writeSavedTeams();
        deleteAllTeamsOnline();
    }

    @Override
    public void createAndSaveTeamFrom(GameType gameType, BaseTeamService teamService, TeamType teamType) {
        if (getSavedTeam(gameType, teamService.getTeamName(teamType), teamService.getGenderType(teamType)) == null) {
            createTeam(gameType);
            copyTeam(teamService, mWrappedTeam, teamType);
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

    private void readSavedTeams() {
        Log.i(Tags.SAVED_TEAMS, String.format("Read saved teams from %s", SAVED_TEAMS_FILE));

        try {
            FileInputStream inputStream = mContext.openFileInput(SAVED_TEAMS_FILE);
            mSavedTeams.clear();
            mSavedTeams.addAll(readTeamsStream(inputStream));
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i(Tags.SAVED_TEAMS, String.format("%s saved teams file does not yet exist", SAVED_TEAMS_FILE));
        } catch (JsonParseException | IOException e) {
            Log.e(Tags.SAVED_TEAMS, "Exception while reading teams", e);
        }
    }

    public static List<RecordedTeam> readTeamsStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RECORDED_TEAM_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    private List<RecordedTeam> readTeams(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RECORDED_TEAM_LIST_TYPE);
    }

    // Write saved teams

    private void writeSavedTeams() {
        Log.i(Tags.SAVED_TEAMS, String.format("Write saved teams into %s", SAVED_TEAMS_FILE));
        try {
            FileOutputStream outputStream = mContext.openFileOutput(SAVED_TEAMS_FILE, Context.MODE_PRIVATE);
            writeTeamsStream(outputStream, mSavedTeams);
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e(Tags.SAVED_TEAMS, "Exception while writing teams", e);
        }
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
        for (RecordedTeam localTeam : mSavedTeams) {
            boolean foundRemoteVersion = false;

            for (RecordedTeam remoteTeam : remoteTeamList) {
                if (localTeam.getName().equals(remoteTeam.getName()) && localTeam.getGenderType().equals(remoteTeam.getGenderType()) && localTeam.getGameType().equals(remoteTeam.getGameType())) {
                    foundRemoteVersion = true;

                    if (localTeam.getDate() < remoteTeam.getDate()) {
                        localTeam.setAll(remoteTeam);
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

            for (RecordedTeam localTeam : mSavedTeams) {
                if (localTeam.getName().equals(remoteTeam.getName()) && localTeam.getGenderType().equals(remoteTeam.getGenderType()) && localTeam.getGameType().equals(remoteTeam.getGameType())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                mSavedTeams.add(remoteTeam);
            }
        }

        writeSavedTeams();
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
