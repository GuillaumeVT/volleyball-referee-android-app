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
import com.tonkar.volleyballreferee.engine.stored.api.ApiPage;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPlayer;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeam;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeamSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserToken;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUtils;
import com.tonkar.volleyballreferee.engine.stored.database.AppDatabase;
import com.tonkar.volleyballreferee.engine.stored.database.TeamEntity;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.definition.BeachTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.EmptyTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.SnowTeamDefinition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class StoredTeamsManager implements StoredTeamsService {

    private final Context mContext;

    public StoredTeamsManager(Context context) {
        mContext = context;
    }

    @Override
    public List<ApiTeamSummary> listTeams() {
        return AppDatabase.getInstance(mContext).teamDao().listTeams();
    }

    @Override
    public List<ApiTeamSummary> listTeams(GameType kind) {
        return AppDatabase.getInstance(mContext).teamDao().listTeamsByKind(kind);
    }

    @Override
    public List<ApiTeamSummary> listTeams(GameType kind, GenderType genderType) {
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
        String userId = PrefUtils.getUser(mContext).getId();

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
            case SNOW:
                teamDefinition = new SnowTeamDefinition(id, userId, TeamType.HOME);
                break;
            case TIME:
            default:
                teamDefinition = new EmptyTeamDefinition(id, userId, TeamType.HOME);
                break;
        }

        return new WrappedTeam(teamDefinition);
    }

    @Override
    public IBaseTeam createTeam(GameType kind) {
        return createWrappedTeam(kind);
    }

    @Override
    public void saveTeam(IBaseTeam team, boolean create) {
        ApiTeam savedTeam = copyTeam(team);
        savedTeam.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        insertTeamIntoDb(savedTeam, false, false);
        pushTeamToServer(savedTeam, create);
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
    public void deleteTeams(Set<String> ids, DataSynchronizationListener listener) {
        new Thread() {
            public void run() {
                for (String id : ids) {
                    AppDatabase.getInstance(mContext).teamDao().deleteById(id);
                    deleteTeamOnServer(id);
                }
                if (listener != null) {
                    listener.onSynchronizationSucceeded();
                }
            }
        }.start();
    }

    @Override
    public void createAndSaveTeamFrom(GameType kind, IBaseTeam teamService, TeamType teamType) {
        if (teamService.getTeamName(teamType).length() > 1 && !GameType.TIME.equals(kind)
                && AppDatabase.getInstance(mContext).teamDao().countByNameAndGenderAndKind(teamService.getTeamName(teamType), teamService.getGender(teamType), kind) == 0
                && AppDatabase.getInstance(mContext).teamDao().countById(teamService.getTeamId(teamType)) == 0) {
            IBaseTeam team = createTeam(kind);
            copyTeam(teamService, team, teamType);
            saveTeam(team, true);
        }
    }

    @Override
    public ApiTeam copyTeam(IBaseTeam teamService) {
        ApiTeam team = new ApiTeam();
        copyTeam(teamService, team, TeamType.HOME);
        return team;
    }

    @Override
    public IBaseTeam copyTeam(ApiTeam team) {
        IBaseTeam teamService = createTeam(team.getKind());
        copyTeam(team, teamService, TeamType.HOME);
        return teamService;
    }

    @Override
    public void copyTeam(ApiTeam source, IBaseTeam dest, TeamType teamType) {
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
            dest.setPlayerName(teamType, player.getNum(), player.getName());
        }
        for (ApiPlayer player : source.getLiberos()) {
            dest.addPlayer(teamType, player.getNum());
            dest.setPlayerName(teamType, player.getNum(), player.getName());
            dest.addLibero(teamType, player.getNum());
        }

        dest.setCaptain(teamType, source.getCaptain());
    }

    @Override
    public void copyTeam(IBaseTeam source, ApiTeam dest, TeamType teamType) {
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

    private void copyTeam(IBaseTeam source, IBaseTeam dest, TeamType teamType) {
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
            dest.setPlayerName(teamType, player.getNum(), player.getName());
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
            return JsonIOUtils.GSON.fromJson(reader, new TypeToken<List<ApiTeam>>(){}.getType());
        }
    }

    @Override
    public ApiTeam readTeam(String json) {
        return JsonIOUtils.GSON.fromJson(json, ApiTeam.class);
    }

    private ApiPage<ApiTeamSummary> readTeams(String json) {
        return JsonIOUtils.GSON.fromJson(json, new TypeToken<ApiPage<ApiTeamSummary>>(){}.getType());
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
        JsonIOUtils.GSON.toJson(teams, new TypeToken<List<ApiTeam>>(){}.getType(), writer);
        writer.close();
    }

    @Override
    public String writeTeam(ApiTeam team) {
        return JsonIOUtils.GSON.toJson(team, ApiTeam.class);
    }

    // Web

    @Override
    public void syncTeams() {
        syncTeams(null);
    }

    @Override
    public void syncTeams(final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            syncTeams(new ArrayList<>(), 0, 100, listener);
        } else {
            if (listener != null){
                listener.onSynchronizationFailed();
            }
        }
    }

    private void syncTeams(List<ApiTeamSummary> remoteTeamList, int page, int size, DataSynchronizationListener listener) {
        Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/teams", ApiUtils.BASE_URL), page, size, PrefUtils.getUserToken(mContext));

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
                    ApiPage<ApiTeamSummary> teamsPage = readTeams(response.body().string());
                    remoteTeamList.addAll(teamsPage.getContent());
                    if (teamsPage.isLast()) {
                        syncTeams(remoteTeamList, listener);
                    } else {
                        syncTeams(remoteTeamList, page + 1, size, listener);
                    }
                } else {
                    Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", response.code()));
                    if (listener != null){
                        listener.onSynchronizationFailed();
                    }
                }
            }
        });
    }

    private void syncTeams(List<ApiTeamSummary> remoteTeamList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getUser(mContext).getId();
        List<ApiTeamSummary> localTeamList = listTeams();
        Queue<ApiTeamSummary> remoteTeamsToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiTeamSummary localTeam : localTeamList) {
            if (localTeam.getCreatedBy().equals(ApiUserSummary.VBR_USER_ID)) {
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

        for (ApiTeamSummary localTeam : localTeamList) {
            boolean foundRemoteVersion = false;

            for (ApiTeamSummary remoteTeam : remoteTeamList) {
                if (localTeam.getId().equals(remoteTeam.getId())) {
                    foundRemoteVersion = true;

                    if (localTeam.getUpdatedAt() < remoteTeam.getUpdatedAt()) {
                        remoteTeamsToDownload.add(remoteTeam);
                    } else if (localTeam.getUpdatedAt() > remoteTeam.getUpdatedAt()) {
                        ApiTeam team = getTeam(localTeam.getId());
                        pushTeamToServer(team, false);
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
                    pushTeamToServer(team, true);
                }
            }
        }

        for (ApiTeamSummary remoteTeam : remoteTeamList) {
            boolean foundLocalVersion = false;

            for (ApiTeamSummary localTeam : localTeamList) {
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

    private void downloadTeamsRecursive(final Queue<ApiTeamSummary> remoteTeams, final DataSynchronizationListener listener) {
        if (remoteTeams.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiTeamSummary remoteTeam = remoteTeams.poll();
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/teams/%s", ApiUtils.BASE_URL, remoteTeam.getId()), PrefUtils.getUserToken(mContext));

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
                        ApiTeam team = readTeam(response.body().string());
                        insertTeamIntoDb(team, true, false);
                        downloadTeamsRecursive(remoteTeams, listener);
                    } else {
                        Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", response.code()));
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }

    private void pushTeamToServer(final ApiTeam team, boolean create) {
        if (PrefUtils.canSync(mContext)) {
            final ApiUserToken userToken = PrefUtils.getUserToken(mContext);
            final String teamStr = writeTeam(team);

            Request request = create ?
                    ApiUtils.buildPost(String.format(Locale.US, "%s/teams", ApiUtils.BASE_URL), teamStr, userToken) :
                    ApiUtils.buildPut(String.format(Locale.US, "%s/teams", ApiUtils.BASE_URL), teamStr, userToken);

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED || response.code() == HttpURLConnection.HTTP_OK) {
                        insertTeamIntoDb(team, true, false);
                    } else {
                        Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while sending team", response.code()));
                    }
                }
            });
        }
    }

    private void deleteTeamOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildDelete(String.format(Locale.US, "%s/teams/%s", ApiUtils.BASE_URL, id), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
                        Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while deleting team", response.code()));
                    }
                }
            });
        }
    }

}
