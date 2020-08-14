package com.tonkar.volleyballreferee.engine.stored;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.game.BaseGame;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.GeneralListener;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.ITimeBasedGame;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionListener;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.game.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.engine.stored.api.ApiCourt;
import com.tonkar.volleyballreferee.engine.stored.api.ApiGame;
import com.tonkar.volleyballreferee.engine.stored.api.ApiGameSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPage;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPlayer;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSelectedLeague;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSet;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSubstitution;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeam;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTimeout;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserToken;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUtils;
import com.tonkar.volleyballreferee.engine.stored.database.AppDatabase;
import com.tonkar.volleyballreferee.engine.stored.database.FullGameEntity;
import com.tonkar.volleyballreferee.engine.stored.database.GameEntity;
import com.tonkar.volleyballreferee.engine.team.IClassicTeam;
import com.tonkar.volleyballreferee.engine.team.TeamListener;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class StoredGamesManager implements StoredGamesService, GeneralListener, ScoreListener, TeamListener, TimeoutListener, SanctionListener {

    private static final String sCurrentGame = "current";
    private static final String sSetupGame   = "setup";

    private final Context    mContext;
    private       IGame      mGame;
    private       StoredGame mStoredGame;

    public StoredGamesManager(Context context) {
        mContext = context;
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
        return AppDatabase.getInstance(mContext).gameDao().listGames();
    }

    @Override
    public IStoredGame getCurrentGame() {
        return mStoredGame;
    }

    @Override
    public IStoredGame getGame(String id) {
        String jsonGame = AppDatabase.getInstance(mContext).gameDao().findContentById(id);
        return readGame(jsonGame);
    }

    @Override
    public void deleteGame(String id) {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).gameDao().deleteById(id);
                deleteGameOnServer(id);
            }
        }.start();
    }

    @Override
    public void deleteGames(Set<String> ids, DataSynchronizationListener listener) {
        new Thread() {
            public void run() {
                for (String id : ids) {
                    AppDatabase.getInstance(mContext).gameDao().deleteById(id);
                    deleteGameOnServer(id);
                }
                if (listener != null) {
                    listener.onSynchronizationSucceeded();
                }
            }
        }.start();
    }

    @Override
    public boolean hasCurrentGame() {
        return AppDatabase.getInstance(mContext).fullGameDao().countByType(sCurrentGame) > 0;
    }

    @Override
    public IGame loadCurrentGame() {
        String jsonGame = AppDatabase.getInstance(mContext).fullGameDao().findContentByType(sCurrentGame);
        return readCurrentGame(jsonGame);
    }

    @Override
    public synchronized void saveCurrentGame(boolean syncInsertion) {
        updateCurrentGame();
        if (!mGame.isMatchCompleted()) {
            insertCurrentGameIntoDb(sCurrentGame, mGame, syncInsertion);
        }
    }

    @Override
    public synchronized void saveCurrentGame() {
        saveCurrentGame(false);
    }

    @Override
    public void deleteCurrentGame() {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).fullGameDao().deleteByType(sCurrentGame);
                mStoredGame = null;
            }
        }.start();
    }

    @Override
    public boolean hasSetupGame() {
        return AppDatabase.getInstance(mContext).fullGameDao().countByType(sSetupGame) > 0;
    }

    @Override
    public IGame loadSetupGame() {
        String jsonGame = AppDatabase.getInstance(mContext).fullGameDao().findContentByType(sSetupGame);
        return readCurrentGame(jsonGame);
    }

    @Override
    public void saveSetupGame(IGame game) {
        insertCurrentGameIntoDb(sSetupGame, game, true);
    }

    @Override
    public void deleteSetupGame() {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).fullGameDao().deleteByType(sSetupGame);
            }
        }.start();
    }

    @Override
    public boolean isGameIndexed(String id) {
        return AppDatabase.getInstance(mContext).gameDao().isGameIndexed(id);
    }

    @Override
    public void toggleGameIndexed(String id, DataSynchronizationListener listener) {
        IStoredGame storedGame = getGame(id);

        if (storedGame == null) {
            listener.onSynchronizationFailed();
        } else {
            storedGame.setIndexed(!storedGame.isIndexed());
            setIndexedOnServer(storedGame, listener);
            insertGameIntoDb((StoredGame) storedGame, false, true);
        }
    }

    @Override
    public void onMatchCompleted(TeamType winner) {
        updateCurrentGame();
        if (mStoredGame != null) {
            insertGameIntoDb(mStoredGame, false, true);
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

                    set.setActingCaptain(TeamType.HOME, indoorTeam.getActingCaptain(TeamType.HOME, setIndex));
                    set.setActingCaptain(TeamType.GUEST, indoorTeam.getActingCaptain(TeamType.GUEST, setIndex));
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

    // Read current game

    private IGame readCurrentGame(String jsonGame) {
        return JsonIOUtils.GSON.fromJson(jsonGame, new TypeToken<BaseGame>(){}.getType());
    }

    // Write current game

    private void insertCurrentGameIntoDb(final String type, IGame game, boolean syncInsertion) {
        String json = writeCurrentGame(game);
        final FullGameEntity fullGameEntity = new FullGameEntity(type, json);

        if (syncInsertion) {
            AppDatabase.getInstance(mContext).fullGameDao().insert(fullGameEntity);
        } else {
            new Thread() {
                public void run() {
                    AppDatabase.getInstance(mContext).fullGameDao().insert(fullGameEntity);
                }
            }.start();
        }
    }

    private String writeCurrentGame(IGame game) {
        return JsonIOUtils.GSON.toJson(game, new TypeToken<BaseGame>(){}.getType());
    }

    // Read stored games

    @Override
    public boolean hasGames() {
        return AppDatabase.getInstance(mContext).gameDao().count() > 0;
    }

    public static List<StoredGame> readStoredGamesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
            return JsonIOUtils.GSON.fromJson(reader, new TypeToken<List<StoredGame>>(){}.getType());
        }
    }

    public static StoredGame byteArrayToStoredGame(byte[] bytes) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            return JsonIOUtils.GSON.fromJson(reader, StoredGame.class);
        }
    }

    private StoredGame readGame(String json) {
        return JsonIOUtils.GSON.fromJson(json, StoredGame.class);
    }

    // Write stored games

    private void insertGameIntoDb(final ApiGame apiGame, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            apiGame.setScore(buildScore(apiGame));
            GameEntity gameEntity = new GameEntity();
            gameEntity.setId(apiGame.getId());
            gameEntity.setCreatedBy(apiGame.getCreatedBy());
            gameEntity.setCreatedAt(apiGame.getCreatedAt());
            gameEntity.setUpdatedAt(apiGame.getUpdatedAt());
            gameEntity.setScheduledAt(apiGame.getScheduledAt());
            gameEntity.setRefereedBy(apiGame.getRefereedBy());
            gameEntity.setRefereeName(apiGame.getRefereeName());
            gameEntity.setReferee1Name(apiGame.getReferee1Name());
            gameEntity.setReferee2Name(apiGame.getReferee2Name());
            gameEntity.setScorerName(apiGame.getScorerName());
            gameEntity.setKind(apiGame.getKind());
            gameEntity.setGender(apiGame.getGender());
            gameEntity.setUsage(apiGame.getUsage());
            gameEntity.setSynced(synced);
            gameEntity.setIndexed(apiGame.isIndexed());
            if (apiGame.getLeague() == null) {
                gameEntity.setLeagueName("");
                gameEntity.setDivisionName("");
            } else {
                gameEntity.setLeagueName(apiGame.getLeague().getName());
                gameEntity.setDivisionName(apiGame.getLeague().getDivision());
            }
            gameEntity.setHomeTeamName(apiGame.getHomeTeam().getName());
            gameEntity.setGuestTeamName(apiGame.getGuestTeam().getName());
            gameEntity.setHomeSets(apiGame.getHomeSets());
            gameEntity.setGuestSets(apiGame.getGuestSets());
            gameEntity.setScore(apiGame.getScore());
            gameEntity.setContent(writeGame(apiGame));
            AppDatabase.getInstance(mContext).gameDao().insert(gameEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            new Thread(runnable).start();
        }
    }

    private String buildScore(ApiGame game) {
        StringBuilder builder = new StringBuilder();
        for (ApiSet set : game.getSets()) {
            builder.append(UiUtils.formatScoreFromLocale(set.getHomePoints(), set.getGuestPoints(), false)).append("\t\t");
        }

        return builder.toString().trim();
    }

    private String writeGame(ApiGame game) {
        return JsonIOUtils.GSON.toJson(game, ApiGame.class);
    }

    private String writeSet(ApiSet set) {
        return JsonIOUtils.GSON.toJson(set, ApiSet.class);
    }

    public static void writeStoredGamesStream(OutputStream outputStream, List<StoredGame> storedGames) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(storedGames, new TypeToken<List<StoredGame>>(){}.getType(), writer);
        writer.close();
    }

    public static byte[] storedGameToByteArray(StoredGame game) throws JsonParseException {
        return JsonIOUtils.GSON.toJson(game, StoredGame.class).getBytes();
    }

    @Override
    public void downloadGame(final String id, final AsyncGameRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/games/%s", ApiUtils.BASE_URL, id), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        StoredGame storedGame = readGame(response.body().string());
                        listener.onGameReceived(storedGame);
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
            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/games/available", ApiUtils.BASE_URL), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        List<ApiGameSummary> games = readGamesAsList(response.body().string());
                        listener.onAvailableGamesReceived(games);
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
            final String gameStr = writeGameDescription(gameDescription);
            Request request = create ?
                    ApiUtils.buildPost(String.format(Locale.US, "%s/games", ApiUtils.BASE_URL), gameStr, PrefUtils.getUserToken(mContext)) :
                    ApiUtils.buildPut(String.format(Locale.US, "%s/games", ApiUtils.BASE_URL), gameStr, PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            Request request = ApiUtils.buildDelete(String.format(Locale.US, "%s/games/%s", ApiUtils.BASE_URL, id), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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

    private String writeGameDescription(ApiGameSummary game) {
        return JsonIOUtils.GSON.toJson(game, ApiGameSummary.class);
    }

    // Read game descriptions

    private List<ApiGameSummary> readGamesAsList(String json) {
        return JsonIOUtils.GSON.fromJson(json, new TypeToken<List<ApiGameSummary>>(){}.getType());
    }

    private ApiPage<ApiGameSummary> readGamesAsPage(String json) {
        return JsonIOUtils.GSON.fromJson(json, new TypeToken<ApiPage<ApiGameSummary>>(){}.getType());
    }

    private void deleteGameOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            Request request = ApiUtils.buildDelete(String.format(Locale.US, "%s/games/%s", ApiUtils.BASE_URL, id), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while all deleting game", response.code()));
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
        if (PrefUtils.canSync(mContext) && storedGame != null && !storedGame.getTeamId(TeamType.HOME).equals(storedGame.getTeamId(TeamType.GUEST))) {
            final ApiUserToken userToken = PrefUtils.getUserToken(mContext);
            final ApiGame game = (ApiGame) storedGame;
            final String jsonGame = writeGame(game);

            Request request = ApiUtils.buildPut(String.format(Locale.US, "%s/games/full", ApiUtils.BASE_URL), jsonGame, userToken);

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        if (GameStatus.COMPLETED.equals(game.getStatus())) {
                            insertGameIntoDb(game, true, false);
                        }
                    } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        Request request2 = ApiUtils.buildPost(String.format(Locale.US, "%s/games/full", ApiUtils.BASE_URL), jsonGame, userToken);

                        ApiUtils.getInstance().getHttpClient(mContext).newCall(request2).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                call.cancel();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                if (response.code() == HttpURLConnection.HTTP_CREATED) {
                                    if (GameStatus.COMPLETED.equals(game.getStatus())) {
                                        insertGameIntoDb(game, true, false);
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
        if (PrefUtils.canSync(mContext) && mStoredGame != null && !mStoredGame.getHomeTeam().getId().equals(mStoredGame.getGuestTeam().getId())) {
            int setIndex = mStoredGame.currentSetIndex();
            final ApiSet set = mStoredGame.getSets().get(setIndex);
            final String jsonSet = writeSet(set);

            Request request = ApiUtils.buildPatch(String.format(Locale.US, "%s/games/%s/set/%d", ApiUtils.BASE_URL, mStoredGame.getId(), 1 + setIndex), jsonSet, PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            Request request = ApiUtils.buildPatch(String.format(Locale.US, "%s/games/%s/indexed/%b", ApiUtils.BASE_URL, storedGame.getId(), storedGame.isIndexed()), PrefUtils.getUserToken(mContext));

            ApiUtils.getInstance().getHttpClient(mContext).newCall(request).enqueue(new Callback() {
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
            if (listener != null){
                listener.onSynchronizationFailed();
            }
        }
    }

    private void syncGames(List<ApiGameSummary> remoteGameList, int page, int size, DataSynchronizationListener listener) {
        Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/games/completed", ApiUtils.BASE_URL), page, size, PrefUtils.getUserToken(mContext));

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
                    ApiPage<ApiGameSummary> gamesPage = readGamesAsPage(response.body().string());
                    remoteGameList.addAll(gamesPage.getContent());
                    if (gamesPage.isLast()) {
                        syncGames(remoteGameList, listener);
                    } else {
                        syncGames(remoteGameList, page + 1, size, listener);
                    }
                } else {
                    Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while synchronising games", response.code()));
                    if (listener != null){
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
                insertGameIntoDb(game, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localGameList = listGames();
        }

        for (Iterator<ApiGameSummary> localGameIt = localGameList.iterator(); localGameIt.hasNext();) {
            ApiGameSummary localGame = localGameIt.next();
            if (GameType.TIME.equals(localGame.getKind())) {
                localGameIt.remove();
            }
        }

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

            Request request = ApiUtils.buildGet(String.format(Locale.US, "%s/games/%s", ApiUtils.BASE_URL, remoteGame.getId()), PrefUtils.getUserToken(mContext));

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
                        ApiGame game = readGame(response.body().string());
                        insertGameIntoDb(game, true, false);
                        downloadGamesRecursive(remoteGames, listener);
                    } else {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while synchronising games", response.code()));
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
                }
            });
        }
    }
}
