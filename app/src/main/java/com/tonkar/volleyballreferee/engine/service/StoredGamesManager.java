package com.tonkar.volleyballreferee.engine.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.VbrApi;
import com.tonkar.volleyballreferee.engine.api.model.ApiCourt;
import com.tonkar.volleyballreferee.engine.api.model.ApiGame;
import com.tonkar.volleyballreferee.engine.api.model.ApiGameSummary;
import com.tonkar.volleyballreferee.engine.api.model.ApiPage;
import com.tonkar.volleyballreferee.engine.api.model.ApiPlayer;
import com.tonkar.volleyballreferee.engine.api.model.ApiSanction;
import com.tonkar.volleyballreferee.engine.api.model.ApiSelectedLeague;
import com.tonkar.volleyballreferee.engine.api.model.ApiSet;
import com.tonkar.volleyballreferee.engine.api.model.ApiSubstitution;
import com.tonkar.volleyballreferee.engine.api.model.ApiTeam;
import com.tonkar.volleyballreferee.engine.api.model.ApiTimeout;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.database.VbrRepository;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.GeneralListener;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.ITimeBasedGame;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionListener;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.game.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.engine.team.IClassicTeam;
import com.tonkar.volleyballreferee.engine.team.TeamListener;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class StoredGamesManager implements StoredGamesService, GeneralListener, ScoreListener, TeamListener, TimeoutListener, SanctionListener {

    private final Context       mContext;
    private final VbrRepository mRepository;
    private       IGame         mGame;
    private       StoredGame    mStoredGame;

    public StoredGamesManager(Context context) {
        mContext = context;
        mRepository = new VbrRepository(mContext);
    }

    @Override
    public void createCurrentGame(IGame game) {
        mGame = game;

        if (hasSetupGame()) {
            deleteSetupGame();
        }

        createCurrentGame();
        saveCurrentGame(true);
    }

    @Override
    public void connectGameRecorder(IGame game) {
        Log.i(Tags.STORED_GAMES, "Connect the game recorder");

        mGame = game;

        mGame.addGeneralListener(this);
        mGame.addScoreListener(this);
        mGame.addTeamListener(this);
        mGame.addTimeoutListener(this);
        mGame.addSanctionListener(this);

        createCurrentGame();
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    @Override
    public void disconnectGameRecorder(boolean exiting) {
        Log.i(Tags.STORED_GAMES, "Disconnect the game recorder");

        if (exiting) {
            saveCurrentGame();
        }

        mGame.removeGeneralListener(this);
        mGame.removeScoreListener(this);
        mGame.removeTeamListener(this);
        mGame.removeTimeoutListener(this);
        mGame.removeSanctionListener(this);
    }

    @Override
    public List<ApiGameSummary> listGames() {
        return mRepository.listGames();
    }

    @Override
    public IStoredGame getCurrentGame() {
        return mStoredGame;
    }

    @Override
    public IStoredGame getGame(String id) {
        return mRepository.getGame(id);
    }

    @Override
    public void deleteGame(String id) {
        mRepository.deleteGame(id);
        deleteGameOnServer(id);
    }

    @Override
    public void deleteGames(Set<String> ids, DataSynchronizationListener listener) {
        mRepository.deleteGames(ids);
        for (String id : ids) {
            deleteGameOnServer(id);
        }
        if (listener != null) {
            listener.onSynchronizationSucceeded();
        }
    }

    @Override
    public boolean hasCurrentGame() {
        return mRepository.hasCurrentGame();
    }

    @Override
    public IGame loadCurrentGame() {
        return mRepository.getCurrentGame();
    }

    @Override
    public synchronized void saveCurrentGame(boolean syncInsertion) {
        updateCurrentGame();
        if (!mGame.isMatchCompleted()) {
            mRepository.insertCurrentGame(mGame, syncInsertion);
        }
    }

    @Override
    public synchronized void saveCurrentGame() {
        saveCurrentGame(false);
    }

    @Override
    public void deleteCurrentGame() {
        mRepository.deleteCurrentGame();
        mStoredGame = null;
    }

    @Override
    public boolean hasSetupGame() {
        return mRepository.hasSetupGame();
    }

    @Override
    public IGame loadSetupGame() {
        return mRepository.getSetupGame();
    }

    @Override
    public void saveSetupGame(IGame game) {
        mRepository.insertSetupGame(game, true);
    }

    @Override
    public void deleteSetupGame() {
        mRepository.deleteSetupGame();
    }

    @Override
    public boolean isGameIndexed(String id) {
        return mRepository.isGameIndexed(id);
    }

    @Override
    public void toggleGameIndexed(String id, DataSynchronizationListener listener) {
        IStoredGame storedGame = getGame(id);

        if (storedGame == null) {
            listener.onSynchronizationFailed();
        } else {
            storedGame.setIndexed(!storedGame.isIndexed());
            setIndexedOnServer(storedGame, listener);
            mRepository.insertGame((StoredGame) storedGame, false, true);
        }
    }

    @Override
    public void onMatchCompleted(TeamType winner) {
        updateCurrentGame();
        if (mStoredGame != null) {
            mRepository.insertGame(mStoredGame, false, true);
        }
        pushCurrentGameToServer();
        deleteCurrentGame();
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        saveCurrentGame();
        pushCurrentSetToServer();
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType, boolean isStart) {}

    @Override
    public void onSetStarted() {
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    @Override
    public void onSetCompleted() {
        saveCurrentGame(true);
    }

    @Override
    public void onStartingLineupSubmitted(TeamType teamType) {
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        saveCurrentGame();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        saveCurrentGame();
        pushCurrentSetToServer();
    }

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {
        saveCurrentGame();
        pushCurrentSetToServer();
    }

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    @Override
    public void onTimeout(TeamType teamType, int duration) {
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    @Override
    public void onTechnicalTimeout(int duration) {
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    @Override
    public void onGameInterval(int duration) {}

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    @Override
    public void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number) {
        saveCurrentGame();
        pushCurrentGameToServer();
    }

    private void createCurrentGame() {
        mStoredGame = new StoredGame();
        mStoredGame.setId(mGame.getId());
        mStoredGame.setCreatedBy(mGame.getCreatedBy());
        mStoredGame.setCreatedAt(mGame.getCreatedAt());
        mStoredGame.setUpdatedAt(mGame.getUpdatedAt());
        mStoredGame.setScheduledAt(mGame.getScheduledAt());
        mStoredGame.setRefereedBy(mGame.getRefereedBy());
        mStoredGame.setRefereeName(mGame.getRefereeName());
        mStoredGame.setReferee1Name(mGame.getReferee1Name());
        mStoredGame.setReferee2Name(mGame.getReferee2Name());
        mStoredGame.setScorerName(mGame.getScorerName());
        mStoredGame.setKind(mGame.getKind());
        mStoredGame.setGender(mGame.getGender());
        mStoredGame.setUsage(mGame.getUsage());
        mStoredGame.setStatus(mGame.getMatchStatus());
        mStoredGame.setIndexed(mGame.isIndexed());
        if (mGame.getLeague() != null && mGame.getKind().equals(mGame.getLeague().getKind()) && mGame.getLeague().getName().length() > 1 && mGame.getLeague().getDivision().length() > 1) {
            ApiSelectedLeague league = new ApiSelectedLeague();
            league.setAll(mGame.getLeague());
            mStoredGame.setLeague(league);
        } else {
            mStoredGame.setLeague(null);
        }

        ApiTeam homeTeam = mStoredGame.getTeam(TeamType.HOME);
        homeTeam.setId(mGame.getTeamId(TeamType.HOME));
        homeTeam.setCreatedBy(mGame.getCreatedBy(TeamType.HOME));
        homeTeam.setCreatedAt(mGame.getCreatedAt(TeamType.HOME));
        homeTeam.setUpdatedAt(mGame.getUpdatedAt(TeamType.HOME));
        homeTeam.setKind(mGame.getTeamsKind());
        homeTeam.setGender(mGame.getGender(TeamType.HOME));
        homeTeam.setName(mGame.getTeamName(TeamType.HOME));
        homeTeam.setColorInt(mGame.getTeamColor(TeamType.HOME));
        homeTeam.setLiberoColorInt(mGame.getLiberoColor(TeamType.HOME));
        homeTeam.setCaptain(mGame.getCaptain(TeamType.HOME));
        homeTeam.setCoach(mGame.getCoachName(TeamType.HOME));

        for (ApiPlayer player : mGame.getPlayers(TeamType.HOME)) {
            if (mGame.isLibero(TeamType.HOME, player.getNum())) {
                homeTeam.getLiberos().add(player);
            } else {
                homeTeam.getPlayers().add(player);
            }
        }

        ApiTeam guestTeam = mStoredGame.getTeam(TeamType.GUEST);
        guestTeam.setId(mGame.getTeamId(TeamType.GUEST));
        guestTeam.setCreatedBy(mGame.getCreatedBy(TeamType.GUEST));
        guestTeam.setCreatedAt(mGame.getCreatedAt(TeamType.GUEST));
        guestTeam.setUpdatedAt(mGame.getUpdatedAt(TeamType.GUEST));
        guestTeam.setKind(mGame.getTeamsKind());
        guestTeam.setGender(mGame.getGender(TeamType.GUEST));
        guestTeam.setName(mGame.getTeamName(TeamType.GUEST));
        guestTeam.setColorInt(mGame.getTeamColor(TeamType.GUEST));
        guestTeam.setLiberoColorInt(mGame.getLiberoColor(TeamType.GUEST));
        guestTeam.setCaptain(mGame.getCaptain(TeamType.GUEST));
        guestTeam.setCoach(mGame.getCoachName(TeamType.GUEST));

        for (ApiPlayer player : mGame.getPlayers(TeamType.GUEST)) {
            if (mGame.isLibero(TeamType.GUEST, player.getNum())) {
                guestTeam.getLiberos().add(player);
            } else {
                guestTeam.getPlayers().add(player);
            }
        }

        mStoredGame.setRules(mGame.getRules());

        updateCurrentGame();
    }

    private void updateCurrentGame() {
        if (mStoredGame != null) {
            mStoredGame.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
            mStoredGame.setMatchStatus(mGame.isMatchCompleted() ? GameStatus.COMPLETED : GameStatus.LIVE);
            mStoredGame.setIndexed(mGame.isIndexed());
            mStoredGame.setSets(TeamType.HOME, mGame.getSets(TeamType.HOME));
            mStoredGame.setSets(TeamType.GUEST, mGame.getSets(TeamType.GUEST));
            mStoredGame.setScore(mGame.getScore());
            mStoredGame.setStartTime(mGame.getStartTime());
            mStoredGame.setEndTime(mGame.getEndTime());

            mStoredGame.getSets().clear();

            for (int setIndex = 0; setIndex < mGame.getNumberOfSets(); setIndex++) {
                ApiSet set = new ApiSet();

                set.setDuration(mGame.getSetDuration(setIndex));
                set.setStartTime(mGame.getSetStartTime(setIndex));
                set.setEndTime(mGame.getSetEndTime(setIndex));
                set.getLadder().addAll(mGame.getPointsLadder(setIndex));
                set.setServing(mGame.getServingTeam(setIndex));
                set.setFirstServing(mGame.getFirstServingTeam(setIndex));

                set.setPoints(TeamType.HOME, mGame.getPoints(TeamType.HOME, setIndex));
                set.setTimeouts(TeamType.HOME, mGame.countRemainingTimeouts(TeamType.HOME, setIndex));

                ApiCourt homeCurrentPlayers = set.getCurrentPlayers(TeamType.HOME);
                for (PositionType position : PositionType.listPositions(mGame.getKind())) {
                    homeCurrentPlayers.setPlayerAt(mGame.getPlayerAtPosition(TeamType.HOME, position, setIndex), position);
                }

                for (ApiTimeout timeout : mGame.getCalledTimeouts(TeamType.HOME, setIndex)) {
                    set.getCalledTimeouts(TeamType.HOME).add(new ApiTimeout(timeout.getHomePoints(), timeout.getGuestPoints()));
                }

                set.setPoints(TeamType.GUEST, mGame.getPoints(TeamType.GUEST, setIndex));
                set.setTimeouts(TeamType.GUEST, mGame.countRemainingTimeouts(TeamType.GUEST, setIndex));

                ApiCourt guestCurrentPlayers = set.getCurrentPlayers(TeamType.GUEST);
                for (PositionType position : PositionType.listPositions(mGame.getKind())) {
                    guestCurrentPlayers.setPlayerAt(mGame.getPlayerAtPosition(TeamType.GUEST, position, setIndex), position);
                }

                for (ApiTimeout timeout : mGame.getCalledTimeouts(TeamType.GUEST, setIndex)) {
                    set.getCalledTimeouts(TeamType.GUEST).add(new ApiTimeout(timeout.getHomePoints(), timeout.getGuestPoints()));
                }

                if (GameType.TIME.equals(mStoredGame.getKind()) && mGame instanceof ITimeBasedGame) {
                    ITimeBasedGame timeBasedGameService = (ITimeBasedGame) mGame;
                    set.setRemainingTime(timeBasedGameService.getRemainingTime(setIndex));
                }

                if (mGame instanceof IClassicTeam) {
                    IClassicTeam indoorTeam = (IClassicTeam) mGame;

                    ApiCourt homeStartingPlayers = set.getStartingPlayers(TeamType.HOME);
                    for (PositionType position : PositionType.listPositions(mGame.getKind())) {
                        homeStartingPlayers.setPlayerAt(mGame.getPlayerAtPositionInStartingLineup(TeamType.HOME, position, setIndex), position);
                    }

                    for (ApiSubstitution substitution : indoorTeam.getSubstitutions(TeamType.HOME, setIndex)) {
                        set.getSubstitutions(TeamType.HOME).add(new ApiSubstitution(substitution.getPlayerIn(), substitution.getPlayerOut(), substitution.getHomePoints(), substitution.getGuestPoints()));
                    }

                    ApiCourt guestStartingPlayers = set.getStartingPlayers(TeamType.GUEST);
                    for (PositionType position : PositionType.listPositions(mGame.getKind())) {
                        guestStartingPlayers.setPlayerAt(mGame.getPlayerAtPositionInStartingLineup(TeamType.GUEST, position, setIndex), position);
                    }

                    for (ApiSubstitution substitution : indoorTeam.getSubstitutions(TeamType.GUEST, setIndex)) {
                        set.getSubstitutions(TeamType.GUEST).add(new ApiSubstitution(substitution.getPlayerIn(), substitution.getPlayerOut(), substitution.getHomePoints(), substitution.getGuestPoints()));
                    }

                    set.setGameCaptain(TeamType.HOME, indoorTeam.getGameCaptain(TeamType.HOME, setIndex));
                    set.setGameCaptain(TeamType.GUEST, indoorTeam.getGameCaptain(TeamType.GUEST, setIndex));
                }

                mStoredGame.getSets().add(set);
            }

            mStoredGame.getAllSanctions(TeamType.HOME).clear();

            for (ApiSanction sanction : mGame.getAllSanctions(TeamType.HOME)) {
                mStoredGame.getAllSanctions(TeamType.HOME).add(new ApiSanction(sanction.getCard(), sanction.getNum(), sanction.getSet(), sanction.getHomePoints(), sanction.getGuestPoints()));
            }

            mStoredGame.getAllSanctions(TeamType.GUEST).clear();

            for (ApiSanction sanction : mGame.getAllSanctions(TeamType.GUEST)) {
                mStoredGame.getAllSanctions(TeamType.GUEST).add(new ApiSanction(sanction.getCard(), sanction.getNum(), sanction.getSet(), sanction.getHomePoints(), sanction.getGuestPoints()));
            }
        }
    }

    public static List<StoredGame> readStoredGamesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return JsonConverters.GSON.fromJson(reader, new TypeToken<List<StoredGame>>(){}.getType());
        }
    }

    public static StoredGame byteArrayToStoredGame(byte[] bytes) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            return JsonConverters.GSON.fromJson(reader, StoredGame.class);
        }
    }

    public static void writeStoredGamesStream(OutputStream outputStream, List<StoredGame> storedGames) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        JsonConverters.GSON.toJson(storedGames, new TypeToken<List<StoredGame>>(){}.getType(), writer);
        writer.close();
    }

    public static byte[] storedGameToByteArray(StoredGame game) throws JsonParseException {
        return JsonConverters.GSON.toJson(game, StoredGame.class).getBytes();
    }

    @Override
    public void downloadGame(final String id, final AsyncGameRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().getGame(id, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            StoredGame storedGame = JsonConverters.GSON.fromJson(body.string(), StoredGame.class);
                            listener.onGameReceived(storedGame);
                        }
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d getting game", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void downloadAvailableGames(final AsyncGameRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().getAvailableGames(mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            List<ApiGameSummary> games = JsonConverters.GSON.fromJson(body.string(), new TypeToken<List<ApiGameSummary>>() {
                            }.getType());
                            listener.onAvailableGamesReceived(games);
                        }
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d getting available games list", response.code()));
                        listener.onError(response.code());
                    }
                }
            });
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void scheduleGame(ApiGameSummary gameDescription, final boolean create, final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().scheduleGame(gameDescription, create, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_CREATED || response.code() == HttpURLConnection.HTTP_OK) {
                        if (listener != null) {
                            listener.onSynchronizationSucceeded();
                        }
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while sending the scheduled game", response.code()));
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void cancelGame(String id, DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().deleteGame(id, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                        listener.onSynchronizationSucceeded();
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while canceling the scheduled game", response.code()));
                        listener.onSynchronizationFailed();
                    }
                }
            });
        } else {
            listener.onSynchronizationFailed();
        }
    }

    private void deleteGameOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            VbrApi.getInstance().deleteGame(id, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while deleting game", response.code()));
                    }
                }
            });
        }
    }

    @Override
    public void onMatchIndexed(boolean indexed) {
        saveCurrentGame();

        if (PrefUtils.canSync(mContext)) {
            pushCurrentGameToServer();
        }
    }

    private void pushGameToServer(final IStoredGame storedGame) {
        if (PrefUtils.canSync(mContext) && storedGame != null && !GameType.TIME.equals(storedGame.getKind()) && !storedGame.getTeamId(TeamType.HOME).equals(storedGame.getTeamId(TeamType.GUEST))) {
            ApiGame game = (ApiGame) storedGame;

            VbrApi.getInstance().updateGame(game, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        if (GameStatus.COMPLETED.equals(game.getStatus())) {
                            mRepository.insertGame(game, true, false);
                        }
                    } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        VbrApi.getInstance().createGame(game, mContext, new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                call.cancel();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                if (response.code() == HttpURLConnection.HTTP_CREATED) {
                                    if (GameStatus.COMPLETED.equals(game.getStatus())) {
                                        mRepository.insertGame(game, true, false);
                                    }
                                } else {
                                    Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while posting game",  response.code()));
                                }
                            }
                        });
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while pushing game",  response.code()));
                    }
                }
            });
        }
    }

    private synchronized void pushCurrentGameToServer() {
        pushGameToServer(mStoredGame);
    }

    private synchronized void pushCurrentSetToServer() {
        if (PrefUtils.canSync(mContext) && mStoredGame != null && !GameType.TIME.equals(mStoredGame.getKind()) && !mStoredGame.getHomeTeam().getId().equals(mStoredGame.getGuestTeam().getId())) {
            VbrApi.getInstance().updateSet(mStoredGame, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != HttpURLConnection.HTTP_OK) {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while patching set", response.code()));
                        pushCurrentGameToServer();
                    }
                }
            });
        }
    }

    private void setIndexedOnServer(IStoredGame storedGame, DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext) && storedGame != null) {
            VbrApi.getInstance().indexGame(storedGame, mContext, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        listener.onSynchronizationSucceeded();
                    } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while indexing game", response.code()));
                        pushCurrentGameToServer();
                        listener.onSynchronizationSucceeded();
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while indexing game", response.code()));
                        listener.onSynchronizationFailed();
                    }
                }
            });
        } else {
            listener.onSynchronizationFailed();
        }
    }

    @Override
    public void syncGames() {
        syncGames(null);
    }

    @Override
    public void syncGames(final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            syncGames(new ArrayList<>(), 0, 50, listener);
        } else {
            if (listener != null) {
                listener.onSynchronizationFailed();
            }
        }
    }

    private void syncGames(List<ApiGameSummary> remoteGameList, int page, int size, DataSynchronizationListener listener) {
        VbrApi.getInstance().getCompletedGames(page, size, mContext, new Callback() {
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
                        ApiPage<ApiGameSummary> gamesPage = JsonConverters.GSON.fromJson(body.string(), new TypeToken<ApiPage<ApiGameSummary>>() {
                        }.getType());
                        remoteGameList.addAll(gamesPage.getContent());
                        if (gamesPage.isLast()) {
                            syncGames(remoteGameList, listener);
                        } else {
                            syncGames(remoteGameList, page + 1, size, listener);
                        }
                    }
                } else {
                    Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while synchronising games", response.code()));
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }
            }
        });
    }

    private void syncGames(List<ApiGameSummary> remoteGameList, DataSynchronizationListener listener) {
        ApiUserSummary user = PrefUtils.getUser(mContext);
        List<ApiGameSummary> localGameList = listGames();
        Queue<ApiGameSummary> remoteGamesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiGameSummary localGame : localGameList) {
            if (localGame.getCreatedBy().equals(ApiUserSummary.VBR_USER_ID)) {
                StoredGame game = (StoredGame) getGame(localGame.getId());
                game.setCreatedBy(user.getId());
                game.setRefereedBy(user.getId());
                game.setRefereeName(user.getPseudo());
                game.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                mRepository.insertGame(game, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localGameList = listGames();
        }

        localGameList.removeIf(localGame -> GameType.TIME.equals(localGame.getKind()));

        for (ApiGameSummary localGame : localGameList) {
            boolean foundRemoteVersion = false;

            for (ApiGameSummary remoteGame : remoteGameList) {
                if (localGame.getId().equals(remoteGame.getId())) {
                    foundRemoteVersion = true;

                    if (localGame.getUpdatedAt() < remoteGame.getUpdatedAt()) {
                        remoteGamesToDownload.add(remoteGame);
                    } else if (localGame.getUpdatedAt() > remoteGame.getUpdatedAt()) {
                        StoredGame game = (StoredGame) getGame(localGame.getId());
                        pushGameToServer(game);
                    }
                }
            }

            if (!foundRemoteVersion) {
                if (localGame.isSynced()) {
                    // if the game was synced, then it was deleted from the server and it must be deleted locally
                    deleteGame(localGame.getId());
                } else {
                    // if the game was not synced, then it is missing from the server because sending it must have failed, so send it again
                    StoredGame game = (StoredGame) getGame(localGame.getId());
                    pushGameToServer(game);
                }
            }
        }

        for (ApiGameSummary remoteGame : remoteGameList) {
            boolean foundLocalVersion = false;

            for (ApiGameSummary localGame : localGameList) {
                if (localGame.getId().equals(remoteGame.getId())) {
                    foundLocalVersion = true;
                    break;
                }
            }

            if (!foundLocalVersion) {
                remoteGamesToDownload.add(remoteGame);
            }
        }

        downloadGamesRecursive(remoteGamesToDownload, listener);
    }

    private void downloadGamesRecursive(final Queue<ApiGameSummary> remoteGames, final DataSynchronizationListener listener) {
        if (remoteGames.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiGameSummary remoteGame = remoteGames.poll();
            VbrApi.getInstance().getGame(remoteGame.getId(), mContext, new Callback() {
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
                            ApiGame game = JsonConverters.GSON.fromJson(body.string(), ApiGame.class);
                            mRepository.insertGame(game, true, false);
                            downloadGamesRecursive(remoteGames, listener);
                        }
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while synchronising games", response.code()));
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }
}
