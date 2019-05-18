package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.db.AppDatabase;
import com.tonkar.volleyballreferee.business.data.db.FullGameEntity;
import com.tonkar.volleyballreferee.business.data.db.GameEntity;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.GeneralListener;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.*;

public class StoredGames implements StoredGamesService, GeneralListener, ScoreListener, TeamListener, TimeoutListener, SanctionListener {

    private static final String sCurrentGame = "current";
    private static final String sSetupGame   = "setup";

    private final Context         mContext;
    private       GameService     mGameService;
    private       StoredGame      mStoredGame;
    private final BatteryReceiver mBatteryReceiver;

    public StoredGames(Context context) {
        mContext = context;
        mBatteryReceiver = new BatteryReceiver();
    }

    @Override
    public void createCurrentGame(GameService gameService) {
        mGameService = gameService;

        if (hasSetupGame()) {
            deleteSetupGame();
        }

        createCurrentGame();
        saveCurrentGame(true);
    }

    @Override
    public void connectGameRecorder(GameService gameService) {
        Log.i(Tags.STORED_GAMES, "Connect the game recorder");

        mGameService = gameService;

        mBatteryReceiver.register(mContext);

        mGameService.addGeneralListener(this);
        mGameService.addScoreListener(this);
        mGameService.addTeamListener(this);
        mGameService.addTimeoutListener(this);
        mGameService.addSanctionListener(this);

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

        mGameService.removeGeneralListener(this);
        mGameService.removeScoreListener(this);
        mGameService.removeTeamListener(this);
        mGameService.removeTimeoutListener(this);
        mGameService.removeSanctionListener(this);

        mBatteryReceiver.unregister(mContext);
    }

    @Override
    public List<ApiGameDescription> listGames() {
        return AppDatabase.getInstance(mContext).gameDao().listGames();
    }

    @Override
    public StoredGameService getCurrentGame() {
        return mStoredGame;
    }

    @Override
    public StoredGameService getGame(String id) {
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
    public void deleteAllGames() {
        new Thread() {
            public void run() {
                AppDatabase.getInstance(mContext).gameDao().deleteAll();
                deleteAllGamesOnServer();
            }
        }.start();
    }

    @Override
    public boolean hasCurrentGame() {
        return AppDatabase.getInstance(mContext).fullGameDao().countByType(sCurrentGame) > 0;
    }

    @Override
    public GameService loadCurrentGame() {
        String jsonGame = AppDatabase.getInstance(mContext).fullGameDao().findContentByType(sCurrentGame);
        return readCurrentGame(jsonGame);
    }

    @Override
    public synchronized void saveCurrentGame(boolean syncInsertion) {
        updateCurrentGame();
        if (!mGameService.isMatchCompleted()) {
            insertCurrentGameIntoDb(sCurrentGame, mGameService, syncInsertion);
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
    public GameService loadSetupGame() {
        String jsonGame = AppDatabase.getInstance(mContext).fullGameDao().findContentByType(sSetupGame);
        return readCurrentGame(jsonGame);
    }

    @Override
    public void saveSetupGame(GameService gameService) {
        insertCurrentGameIntoDb(sSetupGame, gameService, true);
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
        StoredGameService storedGameService = getGame(id);

        if (storedGameService == null) {
            listener.onSynchronizationFailed();
        } else {
            storedGameService.setIndexed(!storedGameService.isIndexed());
            setIndexedOnServer(storedGameService, listener);
            insertGameIntoDb((StoredGame) storedGameService, false, true);
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
    public void onServiceSwapped(TeamType teamType) {}

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
    public void onStartingLineupSubmitted() {
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
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {}

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

    private void createCurrentGame() {
        mStoredGame = new StoredGame();
        mStoredGame.setId(mGameService.getId());
        mStoredGame.setCreatedBy(mGameService.getCreatedBy());
        mStoredGame.setCreatedAt(mGameService.getCreatedAt());
        mStoredGame.setUpdatedAt(mGameService.getUpdatedAt());
        mStoredGame.setScheduledAt(mGameService.getScheduledAt());
        mStoredGame.setRefereedBy(mGameService.getRefereedBy());
        mStoredGame.setRefereeName(mGameService.getRefereeName());
        mStoredGame.setKind(mGameService.getKind());
        mStoredGame.setGender(mGameService.getGender());
        mStoredGame.setUsage(mGameService.getUsage());
        mStoredGame.setStatus(mGameService.getMatchStatus());
        mStoredGame.setIndexed(mGameService.isIndexed());
        if (mGameService.getLeague().getName().length() > 1 && mGameService.getLeague().getDivision().length() > 1) {
            mStoredGame.setLeague(mGameService.getLeague());
        } else {
            mStoredGame.setLeague(null);
        }

        ApiTeam homeTeam = mStoredGame.getTeam(TeamType.HOME);
        homeTeam.setId(mGameService.getTeamId(TeamType.HOME));
        homeTeam.setCreatedBy(mGameService.getCreatedBy(TeamType.HOME));
        homeTeam.setCreatedAt(mGameService.getCreatedAt(TeamType.HOME));
        homeTeam.setUpdatedAt(mGameService.getUpdatedAt(TeamType.HOME));
        homeTeam.setKind(mGameService.getTeamsKind());
        homeTeam.setGender(mGameService.getGender(TeamType.HOME));
        homeTeam.setName(mGameService.getTeamName(TeamType.HOME));
        homeTeam.setColorInt(mGameService.getTeamColor(TeamType.HOME));
        homeTeam.setLiberoColorInt(mGameService.getLiberoColor(TeamType.HOME));
        homeTeam.setCaptain(mGameService.getCaptain(TeamType.HOME));

        for (ApiPlayer player : mGameService.getPlayers(TeamType.HOME)) {
            if (mGameService.isLibero(TeamType.HOME, player.getNum())) {
                homeTeam.getLiberos().add(player);
            } else {
                homeTeam.getPlayers().add(player);
            }
        }

        ApiTeam guestTeam = mStoredGame.getTeam(TeamType.GUEST);
        guestTeam.setId(mGameService.getTeamId(TeamType.GUEST));
        guestTeam.setCreatedBy(mGameService.getCreatedBy(TeamType.GUEST));
        guestTeam.setCreatedAt(mGameService.getCreatedAt(TeamType.GUEST));
        guestTeam.setUpdatedAt(mGameService.getUpdatedAt(TeamType.GUEST));
        guestTeam.setKind(mGameService.getTeamsKind());
        guestTeam.setGender(mGameService.getGender(TeamType.GUEST));
        guestTeam.setName(mGameService.getTeamName(TeamType.GUEST));
        guestTeam.setColorInt(mGameService.getTeamColor(TeamType.GUEST));
        guestTeam.setLiberoColorInt(mGameService.getLiberoColor(TeamType.GUEST));
        guestTeam.setCaptain(mGameService.getCaptain(TeamType.GUEST));

        for (ApiPlayer player : mGameService.getPlayers(TeamType.GUEST)) {
            if (mGameService.isLibero(TeamType.GUEST, player.getNum())) {
                guestTeam.getLiberos().add(player);
            } else {
                guestTeam.getPlayers().add(player);
            }
        }

        mStoredGame.setRules(mGameService.getRules());

        updateCurrentGame();
    }

    private void updateCurrentGame() {
        if (mStoredGame != null) {
            mStoredGame.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
            mStoredGame.setMatchStatus(mGameService.isMatchCompleted() ? GameStatus.COMPLETED : GameStatus.LIVE);
            mStoredGame.setIndexed(mGameService.isIndexed());
            mStoredGame.setSets(TeamType.HOME, mGameService.getSets(TeamType.HOME));
            mStoredGame.setSets(TeamType.GUEST, mGameService.getSets(TeamType.GUEST));
            mStoredGame.setScore(mGameService.getScore());

            mStoredGame.getSets().clear();

            for (int setIndex = 0 ; setIndex < mGameService.getNumberOfSets(); setIndex++) {
                ApiSet set = new ApiSet();

                set.setDuration(mGameService.getSetDuration(setIndex));
                set.getLadder().addAll(mGameService.getPointsLadder(setIndex));
                set.setServing(mGameService.getServingTeam(setIndex));
                set.setFirstServing(mGameService.getFirstServingTeam(setIndex));

                set.setPoints(TeamType.HOME, mGameService.getPoints(TeamType.HOME, setIndex));
                set.setTimeouts(TeamType.HOME, mGameService.getRemainingTimeouts(TeamType.HOME, setIndex));

                ApiCourt homeCurrentPlayers = set.getCurrentPlayers(TeamType.HOME);
                for (PositionType position : PositionType.listPositions(mGameService.getKind())) {
                    homeCurrentPlayers.setPlayerAt(mGameService.getPlayerAtPosition(TeamType.HOME, position, setIndex), position);
                }

                for (ApiTimeout timeout : mGameService.getCalledTimeouts(TeamType.HOME, setIndex)) {
                    set.getCalledTimeouts(TeamType.HOME).add(new ApiTimeout(timeout.getHomePoints(), timeout.getGuestPoints()));
                }

                set.setPoints(TeamType.GUEST, mGameService.getPoints(TeamType.GUEST, setIndex));
                set.setTimeouts(TeamType.GUEST, mGameService.getRemainingTimeouts(TeamType.GUEST, setIndex));

                ApiCourt guestCurrentPlayers = set.getCurrentPlayers(TeamType.GUEST);
                for (PositionType position : PositionType.listPositions(mGameService.getKind())) {
                    guestCurrentPlayers.setPlayerAt(mGameService.getPlayerAtPosition(TeamType.GUEST, position, setIndex), position);
                }

                for (ApiTimeout timeout : mGameService.getCalledTimeouts(TeamType.GUEST, setIndex)) {
                    set.getCalledTimeouts(TeamType.GUEST).add(new ApiTimeout(timeout.getHomePoints(), timeout.getGuestPoints()));
                }

                if (GameType.TIME.equals(mStoredGame.getKind()) && mGameService instanceof TimeBasedGameService) {
                    TimeBasedGameService timeBasedGameService = (TimeBasedGameService) mGameService;
                    set.setRemainingTime(timeBasedGameService.getRemainingTime(setIndex));
                }

                if (mGameService instanceof IndoorTeamService) {
                    IndoorTeamService indoorTeamService = (IndoorTeamService) mGameService;

                    ApiCourt homeStartingPlayers = set.getStartingPlayers(TeamType.HOME);
                    for (PositionType position : PositionType.listPositions(mGameService.getKind())) {
                        homeStartingPlayers.setPlayerAt(mGameService.getPlayerAtPositionInStartingLineup(TeamType.HOME, position, setIndex), position);
                    }

                    for (ApiSubstitution substitution : indoorTeamService.getSubstitutions(TeamType.HOME, setIndex)) {
                        set.getSubstitutions(TeamType.HOME).add(new ApiSubstitution(substitution.getPlayerIn(), substitution.getPlayerOut(), substitution.getHomePoints(), substitution.getGuestPoints()));
                    }

                    ApiCourt guestStartingPlayers = set.getStartingPlayers(TeamType.GUEST);
                    for (PositionType position : PositionType.listPositions(mGameService.getKind())) {
                        guestStartingPlayers.setPlayerAt(mGameService.getPlayerAtPositionInStartingLineup(TeamType.GUEST, position, setIndex), position);
                    }

                    for (ApiSubstitution substitution : indoorTeamService.getSubstitutions(TeamType.GUEST, setIndex)) {
                        set.getSubstitutions(TeamType.GUEST).add(new ApiSubstitution(substitution.getPlayerIn(), substitution.getPlayerOut(), substitution.getHomePoints(), substitution.getGuestPoints()));
                    }

                    set.setActingCaptain(TeamType.HOME, indoorTeamService.getActingCaptain(TeamType.HOME, setIndex));
                    set.setActingCaptain(TeamType.GUEST, indoorTeamService.getActingCaptain(TeamType.GUEST, setIndex));
                }

                mStoredGame.getSets().add(set);
            }

            mStoredGame.getGivenSanctions(TeamType.HOME).clear();

            for (ApiSanction sanction : mGameService.getGivenSanctions(TeamType.HOME)) {
                mStoredGame.getGivenSanctions(TeamType.HOME).add(new ApiSanction(sanction.getCard(), sanction.getNum(), sanction.getSet(), sanction.getHomePoints(), sanction.getGuestPoints()));
            }

            mStoredGame.getGivenSanctions(TeamType.GUEST).clear();

            for (ApiSanction sanction : mGameService.getGivenSanctions(TeamType.GUEST)) {
                mStoredGame.getGivenSanctions(TeamType.GUEST).add(new ApiSanction(sanction.getCard(), sanction.getNum(), sanction.getSet(), sanction.getHomePoints(), sanction.getGuestPoints()));
            }
        }
    }

    // Read current game

    private GameService readCurrentGame(String jsonGame) {
        return JsonIOUtils.GSON.fromJson(jsonGame, JsonIOUtils.CURRENT_GAME_TYPE);
    }

    // Write current game

    private void insertCurrentGameIntoDb(final String type, GameService gameService, boolean syncInsertion) {
        String json = writeCurrentGame(gameService);
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

    private String writeCurrentGame(GameService gameService) {
        return JsonIOUtils.GSON.toJson(gameService, JsonIOUtils.CURRENT_GAME_TYPE);
    }

    // Read stored games

    @Override
    public boolean hasGames() {
        return AppDatabase.getInstance(mContext).gameDao().count() > 0;
    }

    public static List<StoredGame> readStoredGamesStream(InputStream inputStream) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.GAME_LIST_TYPE);
        }
    }

    public static StoredGame byteArrayToStoredGame(byte[] bytes) throws IOException, JsonParseException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.GAME_TYPE);
        }
    }

    private StoredGame readGame(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.GAME_TYPE);
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
        return JsonIOUtils.GSON.toJson(game, JsonIOUtils.GAME_TYPE);
    }

    private String writeSet(ApiSet set) {
        return JsonIOUtils.GSON.toJson(set, JsonIOUtils.SET_TYPE);
    }

    public static void writeStoredGamesStream(OutputStream outputStream, List<StoredGame> storedGames) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(storedGames, JsonIOUtils.GAME_LIST_TYPE, writer);
        writer.close();
    }

    public static byte[] storedGameToByteArray(StoredGame game) throws JsonParseException {
        return JsonIOUtils.GSON.toJson(game, JsonIOUtils.GAME_TYPE).getBytes();
    }

    @Override
    public void downloadGame(final String id, final AsyncGameRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, String.format(ApiUtils.GAME_API_URL, id), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        StoredGame storedGame = readGame(response);

                        if (storedGame == null) {
                            Log.e(Tags.STORED_GAMES, "Failed to deserialize game or to notify the listener");
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        } else {
                            listener.onGameReceived(storedGame);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d getting game", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void downloadAvailableGames(final AsyncGameRequestListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.AVAILABLE_GAMES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiGameDescription> games = readGames(response);

                        if (games == null) {
                            Log.e(Tags.STORED_GAMES, "Failed to deserialize available games or to notify the listener");
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        } else {
                            listener.onAvailableGamesReceived(games);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d getting available games list", error.networkResponse.statusCode));
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            listener.onError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void scheduleGame(ApiGameDescription gameDescription, final boolean create, final DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            final byte[] bytes = writeGameDescription(gameDescription).getBytes();

            int requestMethod = create ? Request.Method.POST : Request.Method.PUT;
            JsonStringRequest stringRequest = new JsonStringRequest(requestMethod, ApiUtils.GAMES_API_URL, bytes, PrefUtils.getAuthentication(mContext),
                    response -> {
                        if (listener != null) {
                            listener.onSynchronizationSucceeded();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while sending the scheduled game", error.networkResponse.statusCode));
                        }
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    @Override
    public void cancelGame(String id, DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, String.format(ApiUtils.GAME_API_URL, id), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> listener.onSynchronizationSucceeded(),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while canceling the scheduled game", error.networkResponse.statusCode));
                        }
                        listener.onSynchronizationFailed();
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        } else {
            listener.onSynchronizationFailed();
        }
    }

    private String writeGameDescription(ApiGameDescription game) {
        return JsonIOUtils.GSON.toJson(game, JsonIOUtils.GAME_DESCRIPTION_TYPE);
    }

    // Read game descriptions

    private List<ApiGameDescription> readGames(String json) {
        return JsonIOUtils.GSON.fromJson(json, JsonIOUtils.GAME_DESCRIPTION_LIST_TYPE);
    }

    private void deleteGameOnServer(final String id) {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, String.format(ApiUtils.GAME_API_URL, id), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while deleting game", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllGamesOnServer() {
        if (PrefUtils.canSync(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, ApiUtils.GAMES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while all deleting games", error.networkResponse.statusCode));
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    @Override
    public void onMatchIndexed(boolean indexed) {
        saveCurrentGame();

        if (PrefUtils.canSync(mContext)) {
            pushCurrentGameToServer();
        }
    }

    private void pushGameToServer(final StoredGameService storedGameService) {
        if (mBatteryReceiver.canPushGameToServer() && PrefUtils.canSync(mContext) && storedGameService != null && isNotTestGame(storedGameService)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            final ApiGame game = (ApiGame) storedGameService;
            final byte[] bytes = writeGame(game).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, ApiUtils.FULL_GAME_API_URL, bytes, authentication,
                    response -> {
                        if (GameStatus.COMPLETED.equals(game.getStatus())) {
                            insertGameIntoDb(game, true, false);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                            JsonStringRequest stringRequest1 = new JsonStringRequest(Request.Method.POST, ApiUtils.FULL_GAME_API_URL, bytes, authentication,
                                    response -> {
                                        if (GameStatus.COMPLETED.equals(game.getStatus())) {
                                            insertGameIntoDb(game, true, false);
                                        }
                                    },
                                    error2 -> {
                                        if (error2.networkResponse != null) {
                                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while creating game", error2.networkResponse.statusCode));
                                        }
                                    }
                            );
                            stringRequest1.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest1);
                        } else {
                            if (error.networkResponse != null) {
                                Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while creating game", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private synchronized void pushCurrentGameToServer() {
        pushGameToServer(mStoredGame);
    }

    private synchronized void pushCurrentSetToServer() {
        if (mBatteryReceiver.canPushSetToServer() && PrefUtils.canSync(mContext) && mStoredGame != null && isNotTestGame(mStoredGame)) {
            int setIndex = mStoredGame.currentSetIndex();
            final ApiSet set = mStoredGame.getSets().get(setIndex);
            final byte[] bytes = writeSet(set).getBytes();

            String url = String.format(ApiUtils.GAME_SET_API_URL, mStoredGame.getId(), 1 + setIndex);
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PATCH, url, bytes, PrefUtils.getAuthentication(mContext),
                    response -> {},
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while uploading set", error.networkResponse.statusCode));
                            if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                pushCurrentGameToServer();
                            }
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void setIndexedOnServer(StoredGameService storedGameService, DataSynchronizationListener listener) {
        if (PrefUtils.canSync(mContext) && storedGameService != null) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);

            String url = String.format(ApiUtils.GAME_INDEXED_API_URL, storedGameService.getId(), storedGameService.isIndexed());
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PATCH, url, new byte[0], authentication,
                    response -> listener.onSynchronizationSucceeded(),
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while indexing game", error.networkResponse.statusCode));
                            if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                pushCurrentGameToServer();
                                listener.onSynchronizationSucceeded();
                            } else {
                                listener.onSynchronizationFailed();
                            }
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
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
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.COMPLETED_GAMES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiGameDescription> gameList = readGames(response);
                        syncGames(gameList, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while synchronising games", error.networkResponse.statusCode));
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

    private void syncGames(List<ApiGameDescription> remoteGameList, DataSynchronizationListener listener) {
        Authentication authentication = PrefUtils.getAuthentication(mContext);
        List<ApiGameDescription> localGameList = listGames();
        Queue<ApiGameDescription> remoteGamesToDownload = new LinkedList<>();
        boolean afterPurchase = false;

        // User purchased web services, write his user id
        for (ApiGameDescription localGame : localGameList) {
            if (localGame.getCreatedBy().equals(Authentication.VBR_USER_ID)) {
                StoredGame game = (StoredGame) getGame(localGame.getId());
                game.setCreatedBy(authentication.getUserId());
                game.setRefereedBy(authentication.getUserId());
                game.setRefereeName(authentication.getUserPseudo());
                game.setUpdatedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
                insertGameIntoDb(game, false, true);
                afterPurchase = true;
            }
        }

        if (afterPurchase) {
            localGameList = listGames();
        }

        for (ApiGameDescription localGame : localGameList) {
            boolean foundRemoteVersion = false;

            for (ApiGameDescription remoteGame : remoteGameList) {
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

        for (ApiGameDescription remoteGame : remoteGameList) {
            boolean foundLocalVersion = false;

            for (ApiGameDescription localGame : localGameList) {
                if (localGame.getId().equals(remoteGame.getId())) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                remoteGamesToDownload.add(remoteGame);
            }
        }

        downloadGamesRecursive(remoteGamesToDownload, listener);
    }

    private void downloadGamesRecursive(final Queue<ApiGameDescription> remoteGames, final DataSynchronizationListener listener) {
        if (remoteGames.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiGameDescription remoteGame = remoteGames.poll();
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, String.format(ApiUtils.GAME_API_URL, remoteGame.getId()), new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        ApiGame game = readGame(response);
                        insertGameIntoDb(game, true, false);
                        downloadGamesRecursive(remoteGames, listener);
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while synchronising games", error.networkResponse.statusCode));
                        }
                        if (listener != null){
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private boolean isNotTestGame(StoredGameService storedGameService) {
        return storedGameService.getTeamName(TeamType.HOME).length() > 1 && storedGameService.getTeamName(TeamType.GUEST).length() > 1;
    }
}
