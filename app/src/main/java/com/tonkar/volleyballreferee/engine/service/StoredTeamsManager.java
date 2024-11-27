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
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.definition.*;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

import okhttp3.*;

public class StoredTeamsManager implements StoredTeamsService {

    private final Context       mContext;
    private final VbrRepository mRepository;

    public StoredTeamsManager(Context context) {
        mContext = context;
        mRepository = new VbrRepository(mContext);
    }

    @Override
    public List<TeamSummaryDto> listTeams() {
        return mRepository.listTeams();
    }

    @Override
    public List<TeamSummaryDto> listTeams(GameType kind) {
        return mRepository.listTeams(kind);
    }

    @Override
    public List<TeamSummaryDto> listTeams(GameType kind, GenderType genderType) {
        return mRepository.listTeams(genderType, kind);
    }

    @Override
    public TeamDto getTeam(String id) {
        return mRepository.getTeam(id);
    }

    @Override
    public TeamDto getTeam(GameType kind, String teamName, GenderType genderType) {
        return mRepository.getTeam(teamName, genderType, kind);
    }

    private WrappedTeam createWrappedTeam(GameType kind) {
        String id = UUID.randomUUID().toString();
        String userId = PrefUtils.getUserId(mContext);

        TeamDefinition teamDefinition = switch (kind) {
            case INDOOR, INDOOR_4X4 -> new IndoorTeamDefinition(kind, id, userId, TeamType.HOME);
            case BEACH -> new BeachTeamDefinition(id, userId, TeamType.HOME);
            case SNOW -> new SnowTeamDefinition(id, userId, TeamType.HOME);
        };

        return new WrappedTeam(teamDefinition);
    }

    @Override
    public IBaseTeam createTeam(GameType kind) {
        return createWrappedTeam(kind);
    }

    @Override
    public void saveTeam(IBaseTeam team, boolean create) {
        TeamDto savedTeam = copyTeam(team);
        savedTeam.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        mRepository.insertTeam(savedTeam, false, false);
        pushTeamToServer(savedTeam, create);
    }

    @Override
    public void deleteTeam(String id) {
        mRepository.deleteTeam(id);
        deleteTeamOnServer(id);
    }

    @Override
    public void deleteTeams(Set<String> ids, DataSynchronizationListener listener) {
        if (!ids.isEmpty()) {
            mRepository.deleteTeams(ids);
            for (String id : ids) {
                deleteTeamOnServer(id);
            }
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        }
    }

    @Override
    public void createAndSaveTeamFrom(GameType kind, IBaseTeam teamService, TeamType teamType) {
        if (teamService.getTeamName(teamType).length() > 1 && mRepository.countTeams(teamService.getTeamName(teamType),
                                                                                     teamService.getGender(teamType),
                                                                                     kind) == 0 && mRepository.countTeams(
                teamService.getTeamId(teamType)) == 0) {
            IBaseTeam team = createTeam(kind);
            copyTeam(teamService, team, teamType);
            saveTeam(team, true);
        }
    }

    @Override
    public TeamDto copyTeam(IBaseTeam teamService) {
        TeamDto team = new TeamDto();
        copyTeam(teamService, team, TeamType.HOME);
        return team;
    }

    @Override
    public IBaseTeam copyTeam(TeamDto team) {
        IBaseTeam teamService = createTeam(team.getKind());
        copyTeam(team, teamService, TeamType.HOME);
        return teamService;
    }

    @Override
    public void copyTeam(TeamDto source, IBaseTeam dest, TeamType teamType) {
        dest.setTeamId(teamType, source.getId());
        dest.setCreatedBy(teamType, source.getCreatedBy());
        dest.setCreatedAt(teamType, source.getCreatedAt());
        dest.setUpdatedAt(teamType, source.getUpdatedAt());
        dest.setTeamName(teamType, source.getName());
        dest.setTeamColor(teamType, source.getColorInt());
        dest.setLiberoColor(teamType, source.getLiberoColorInt());
        dest.setGender(teamType, source.getGender());

        for (PlayerDto player : dest.getPlayers(teamType)) {
            dest.removePlayer(teamType, player.getNum());
        }
        for (PlayerDto player : dest.getLiberos(teamType)) {
            dest.removeLibero(teamType, player.getNum());
        }
        for (PlayerDto player : source.getPlayers()) {
            dest.addPlayer(teamType, player.getNum());
            dest.setPlayerName(teamType, player.getNum(), player.getName());
        }
        for (PlayerDto player : source.getLiberos()) {
            dest.addPlayer(teamType, player.getNum());
            dest.setPlayerName(teamType, player.getNum(), player.getName());
            dest.addLibero(teamType, player.getNum());
        }

        dest.setCaptain(teamType, source.getCaptain());
        dest.setCoachName(teamType, source.getCoach());
    }

    @Override
    public void copyTeam(IBaseTeam source, TeamDto dest, TeamType teamType) {
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

        for (PlayerDto player : source.getPlayers(teamType)) {
            dest.getPlayers().add(player);
        }
        for (PlayerDto player : source.getLiberos(teamType)) {
            dest.getLiberos().add(player);
        }

        dest.setCaptain(source.getCaptain(teamType));
        dest.setCoach(source.getCoachName(teamType));
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

        for (PlayerDto player : dest.getPlayers(teamType)) {
            dest.removePlayer(teamType, player.getNum());
        }
        for (PlayerDto player : dest.getLiberos(teamType)) {
            dest.removeLibero(teamType, player.getNum());
        }

        for (PlayerDto player : source.getPlayers(teamType)) {
            dest.addPlayer(teamType, player.getNum());
            dest.setPlayerName(teamType, player.getNum(), player.getName());
        }
        for (PlayerDto player : source.getLiberos(teamType)) {
            dest.addLibero(teamType, player.getNum());
        }

        dest.setCaptain(teamType, source.getCaptain(teamType));
        dest.setCoachName(teamType, source.getCoachName(teamType));
    }

    public static List<TeamDto> readTeamsStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return JsonConverters.GSON.fromJson(reader, new TypeToken<List<TeamDto>>() {}.getType());
        }
    }

    public static void writeTeamsStream(OutputStream outputStream, List<TeamDto> teams) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        JsonConverters.GSON.toJson(teams, new TypeToken<List<TeamDto>>() {}.getType(), writer);
        writer.close();
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
            if (listener != null) {
                listener.onSynchronizationFailed();
            }
        }
    }

    private void syncTeams(List<TeamSummaryDto> remoteTeamList, int page, int size, DataSynchronizationListener listener) {
        VbrApi.getInstance(mContext).getTeamPage(page, size, mContext, new Callback() {
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
                        PageDto<TeamSummaryDto> teamsPage = JsonConverters.GSON.fromJson(body.string(),
                                                                                         new TypeToken<PageDto<TeamSummaryDto>>() {}.getType());
                        remoteTeamList.addAll(teamsPage.getContent());
                        if (teamsPage.isLast()) {
                            syncTeams(remoteTeamList, listener);
                        } else {
                            syncTeams(remoteTeamList, page + 1, size, listener);
                        }
                    }
                } else {
                    Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", response.code()));
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }
            }
        });
    }

    private void syncTeams(List<TeamSummaryDto> remoteTeamList, DataSynchronizationListener listener) {
        String userId = Optional.ofNullable(PrefUtils.getUser(mContext)).map(UserSummaryDto::getId).orElse(null);
        List<TeamSummaryDto> localTeamList = listTeams();
        Queue<TeamSummaryDto> remoteTeamsToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (TeamSummaryDto localTeam : localTeamList) {
            if (StringUtils.isBlank(localTeam.getCreatedBy())) {
                TeamDto team = getTeam(localTeam.getId());
                team.setCreatedBy(userId);
                team.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                mRepository.insertTeam(team, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localTeamList = listTeams();
        }

        for (TeamSummaryDto localTeam : localTeamList) {
            boolean foundRemoteVersion = false;

            for (TeamSummaryDto remoteTeam : remoteTeamList) {
                if (localTeam.getId().equals(remoteTeam.getId())) {
                    foundRemoteVersion = true;

                    if (localTeam.getUpdatedAt() < remoteTeam.getUpdatedAt()) {
                        remoteTeamsToDownload.add(remoteTeam);
                    } else if (localTeam.getUpdatedAt() > remoteTeam.getUpdatedAt()) {
                        TeamDto team = getTeam(localTeam.getId());
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
                    TeamDto team = getTeam(localTeam.getId());
                    pushTeamToServer(team, true);
                }
            }
        }

        for (TeamSummaryDto remoteTeam : remoteTeamList) {
            boolean foundLocalVersion = false;

            for (TeamSummaryDto localTeam : localTeamList) {
                if (localTeam.getId().equals(remoteTeam.getId())) {
                    foundLocalVersion = true;
                    break;
                }
            }

            if (!foundLocalVersion) {
                remoteTeamsToDownload.add(remoteTeam);
            }
        }

        downloadTeamsRecursive(remoteTeamsToDownload, listener);
    }

    private void downloadTeamsRecursive(final Queue<TeamSummaryDto> remoteTeams, final DataSynchronizationListener listener) {
        if (remoteTeams.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            TeamSummaryDto remoteTeam = remoteTeams.poll();
            VbrApi.getInstance(mContext).getTeam(remoteTeam.getId(), mContext, new Callback() {
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
                            TeamDto team = JsonConverters.GSON.fromJson(body.string(), TeamDto.class);
                            mRepository.insertTeam(team, true, false);
                            downloadTeamsRecursive(remoteTeams, listener);
                        }
                    } else {
                        Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while synchronising teams", response.code()));
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }

    private void pushTeamToServer(final TeamDto team, boolean create) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).upsertTeam(team, create, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED || response.code() == HttpURLConnection.HTTP_OK) {
                        mRepository.insertTeam(team, true, false);
                    } else {
                        Log.e(Tags.STORED_TEAMS, String.format(Locale.getDefault(), "Error %d while sending team", response.code()));
                    }
                }
            });
        }
    }

    private void deleteTeamOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance(mContext).deleteTeam(id, mContext, new Callback() {
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
