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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

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

    private synchronized void saveCurrentGame(boolean syncInsertion) {
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
    public void toggleGameIndexed(String id) {
        StoredGameService storedGameService = getGame(id);

        if (storedGameService != null) {
            storedGameService.setIndexed(!storedGameService.isIndexed());
            pushGameToServer(storedGameService);
            insertGameIntoDb((StoredGame) storedGameService, false);
        }
    }

    @Override
    public void onMatchCompleted(TeamType winner) {
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
        saveCurrentGame();
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
        mStoredGame.setId(mGameService.geId());
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
        mStoredGame.setLeagueId(mGameService.getLeagueId());
        mStoredGame.setLeagueName(mGameService.getLeagueName());
        mStoredGame.setDivisionName(mGameService.getDivisionName());

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

        mStoredGame.getRules().setAll(mGameService.getRules());

        updateCurrentGame();
    }

    private void updateCurrentGame() {
        if (mStoredGame != null) {
            mStoredGame.setUpdatedAt(System.currentTimeMillis());
            mStoredGame.setMatchStatus(mGameService.isMatchCompleted() ? GameStatus.COMPLETED : GameStatus.LIVE);
            mStoredGame.setIndexed(mGameService.isIndexed());
            mStoredGame.setSets(TeamType.HOME, mGameService.getSets(TeamType.HOME));
            mStoredGame.setSets(TeamType.GUEST, mGameService.getSets(TeamType.GUEST));

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
                homeCurrentPlayers.setP1(mGameService.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_1, setIndex));
                homeCurrentPlayers.setP2(mGameService.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_2, setIndex));
                homeCurrentPlayers.setP3(mGameService.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_3, setIndex));
                homeCurrentPlayers.setP4(mGameService.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_4, setIndex));
                homeCurrentPlayers.setP5(mGameService.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_5, setIndex));
                homeCurrentPlayers.setP6(mGameService.getPlayerAtPosition(TeamType.HOME, PositionType.POSITION_6, setIndex));

                for (ApiTimeout timeout : mGameService.getCalledTimeouts(TeamType.HOME, setIndex)) {
                    set.getCalledTimeouts(TeamType.HOME).add(new ApiTimeout(timeout.getHomePoints(), timeout.getGuestPoints()));
                }

                set.setPoints(TeamType.GUEST, mGameService.getPoints(TeamType.GUEST, setIndex));
                set.setTimeouts(TeamType.GUEST, mGameService.getRemainingTimeouts(TeamType.GUEST, setIndex));

                ApiCourt guestCurrentPlayers = set.getCurrentPlayers(TeamType.GUEST);
                guestCurrentPlayers.setP1(mGameService.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_1, setIndex));
                guestCurrentPlayers.setP2(mGameService.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_2, setIndex));
                guestCurrentPlayers.setP3(mGameService.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_3, setIndex));
                guestCurrentPlayers.setP4(mGameService.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_4, setIndex));
                guestCurrentPlayers.setP5(mGameService.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_5, setIndex));
                guestCurrentPlayers.setP6(mGameService.getPlayerAtPosition(TeamType.GUEST, PositionType.POSITION_6, setIndex));

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
                    homeStartingPlayers.setP1(mGameService.getPlayerAtPositionInStartingLineup(TeamType.HOME, PositionType.POSITION_1, setIndex));
                    homeStartingPlayers.setP2(mGameService.getPlayerAtPositionInStartingLineup(TeamType.HOME, PositionType.POSITION_2, setIndex));
                    homeStartingPlayers.setP3(mGameService.getPlayerAtPositionInStartingLineup(TeamType.HOME, PositionType.POSITION_3, setIndex));
                    homeStartingPlayers.setP4(mGameService.getPlayerAtPositionInStartingLineup(TeamType.HOME, PositionType.POSITION_4, setIndex));
                    homeStartingPlayers.setP5(mGameService.getPlayerAtPositionInStartingLineup(TeamType.HOME, PositionType.POSITION_5, setIndex));
                    homeStartingPlayers.setP6(mGameService.getPlayerAtPositionInStartingLineup(TeamType.HOME, PositionType.POSITION_6, setIndex));

                    for (ApiSubstitution substitution : indoorTeamService.getSubstitutions(TeamType.HOME, setIndex)) {
                        set.getSubstitutions(TeamType.HOME).add(new ApiSubstitution(substitution.getPlayerIn(), substitution.getPlayerOut(), substitution.getHomePoints(), substitution.getGuestPoints()));
                    }

                    ApiCourt guestStartingPlayers = set.getStartingPlayers(TeamType.GUEST);
                    guestStartingPlayers.setP1(mGameService.getPlayerAtPositionInStartingLineup(TeamType.GUEST, PositionType.POSITION_1, setIndex));
                    guestStartingPlayers.setP2(mGameService.getPlayerAtPositionInStartingLineup(TeamType.GUEST, PositionType.POSITION_2, setIndex));
                    guestStartingPlayers.setP3(mGameService.getPlayerAtPositionInStartingLineup(TeamType.GUEST, PositionType.POSITION_3, setIndex));
                    guestStartingPlayers.setP4(mGameService.getPlayerAtPositionInStartingLineup(TeamType.GUEST, PositionType.POSITION_4, setIndex));
                    guestStartingPlayers.setP5(mGameService.getPlayerAtPositionInStartingLineup(TeamType.GUEST, PositionType.POSITION_5, setIndex));
                    guestStartingPlayers.setP6(mGameService.getPlayerAtPositionInStartingLineup(TeamType.GUEST, PositionType.POSITION_6, setIndex));

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

    // Read recorded games

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

    // Write recorded games

    private void insertGameIntoDb(final StoredGame recordedGame, boolean synced, boolean syncInsertion) {
        if (syncInsertion) {
            insertGameIntoDb(recordedGame, synced);
        } else {
            new Thread() {
                public void run() {
                    insertGameIntoDb(recordedGame, synced);
                }
            }.start();
        }
    }
    private String buildScore(ApiGame game) {
        StringBuilder scoreBuilder = new StringBuilder();

        for (ApiSet set : game.getSets()) {
            scoreBuilder.append(String.format(Locale.getDefault(), "%d-%d\t\t", set.getHomePoints(), set.getGuestPoints()));
        }

        return scoreBuilder.toString().trim();
    }

    private void insertGameIntoDb(final ApiGame apiGame, boolean synced) {
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
        gameEntity.setSynced(synced);
        gameEntity.setStatus(apiGame.getStatus());
        gameEntity.setUsage(apiGame.getUsage());
        gameEntity.setIndexed(apiGame.isIndexed());
        gameEntity.setLeagueId(apiGame.getLeagueId());
        gameEntity.setLeagueName(apiGame.getLeagueName());
        gameEntity.setDivisionName(apiGame.getDivisionName());
        gameEntity.setHomeTeamId(apiGame.getHomeTeam().getId());
        gameEntity.setHomeTeamName(apiGame.getHomeTeam().getName());
        gameEntity.setGuestTeamId(apiGame.getGuestTeam().getId());
        gameEntity.setGuestTeamName(apiGame.getGuestTeam().getName());
        gameEntity.setHomeSets(apiGame.getHomeSets());
        gameEntity.setGuestSets(apiGame.getGuestSets());
        gameEntity.setRulesId(apiGame.getRules().getId());
        gameEntity.setRulesName(apiGame.getRules().getName());
        gameEntity.setScore(apiGame.getScore());
        gameEntity.setContent(writeGame(apiGame));
        AppDatabase.getInstance(mContext).gameDao().insert(gameEntity);
    }

    private String writeGame(ApiGame game) {
        return JsonIOUtils.GSON.toJson(game, JsonIOUtils.GAME_TYPE);
    }

    private String writeSet(ApiSet set) {
        return JsonIOUtils.GSON.toJson(set, JsonIOUtils.SET_TYPE);
    }

    public static void writeStoredGamesStream(OutputStream outputStream, List<StoredGame> recordedGames) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(recordedGames, JsonIOUtils.GAME_LIST_TYPE, writer);
        writer.close();
    }

    public static byte[] storedGameToByteArray(StoredGame recordedGame) throws JsonParseException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(recordedGame, JsonIOUtils.GAME_TYPE, writer);
        writer.close();

        outputStream.close();

        return outputStream.toByteArray();
    }

    private byte[] recordedSetToByteArray(RecordedSet recordedSet) throws JsonParseException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(recordedSet, JsonIOUtils.RECORDED_SET_TYPE, writer);
        writer.close();

        outputStream.close();

        return outputStream.toByteArray();
    }

    @Override
    public void getGameFromCode(final int code, final AsyncGameRequestListener listener) {
        String url = String.format(Locale.getDefault(), ApiUtils.GAME_CODE_API_URL, code);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, url, new byte[0],
                response -> {
                    StoredGame recordedGame = readGame(response);

                    if (recordedGame == null) {
                        Log.e(Tags.STORED_GAMES, "Failed to deserialize a game from code or to notify the listener");
                        listener.onInternalError();
                    } else {
                        listener.onRecordedGameReceivedFromCode(recordedGame);
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d getting a game from code", error.networkResponse.statusCode));
                        if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                            listener.onNotFound();
                        } else {
                            listener.onError();
                        }
                    } else {
                        listener.onError();
                    }
                }
        );
        ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
    }

    @Override
    public void getUserGame(final long id, final AsyncGameRequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        String parameters = JsonStringRequest.getParameters(params);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.USER_GAME_API_URL + parameters, new byte[0], PrefUtils.getAuthentication(mContext),
                response -> {
                    StoredGame recordedGame = readGame(response);

                    if (recordedGame == null) {
                        Log.e(Tags.STORED_GAMES, "Failed to deserialize a user game or to notify the listener");
                        listener.onInternalError();
                    } else {
                        if (listener != null) {
                            listener.onGameReceived(recordedGame);
                        }
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d getting a user game", error.networkResponse.statusCode));
                        if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                            listener.onNotFound();
                        } else {
                            listener.onError();
                        }
                    } else {
                        listener.onError();
                    }
                }
        );
        ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
    }

    @Override
    public void downloadAvailableGames(final AsyncGameRequestListener listener) {
        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.USER_SCHEDULED_GAMES_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                response -> {
                    List<ApiGameDescription> gameDescriptionList = readGameDescriptionList(response);

                    if (gameDescriptionList == null) {
                        Log.e(Tags.STORED_GAMES, "Failed to deserialize a user scheduled game list or to notify the listener");
                        listener.onInternalError();
                    } else {
                        if (listener != null) {
                            listener.onAvailableGamesReceived(gameDescriptionList);
                        }
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d getting a user scheduled game list", error.networkResponse.statusCode));
                    }
                    listener.onError();
                }
        );
        ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
    }

    @Override
    public void scheduleGame(ApiGameDescription gameDescription, final boolean create, final DataSynchronizationListener listener) {
        if (PrefUtils.isSyncOn(mContext)) {
            try {
                final byte[] bytes = gameDescriptionToByteArray(gameDescription);

                int requestMethod = create ? Request.Method.POST : Request.Method.PUT;
                JsonStringRequest stringRequest = new JsonStringRequest(requestMethod, ApiUtils.USER_GAME_API_URL, bytes, PrefUtils.getAuthentication(mContext),
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
            } catch (JsonParseException | IOException e) {
                Log.e(Tags.STORED_GAMES, "Exception while writing game description", e);
                if (listener != null) {
                    listener.onSynchronizationFailed();
                }
            }
        }
    }

    @Override
    public void cancelGame(String id, DataSynchronizationListener listener) {

    }

    @Override
    public void cancelGame(long id, DataSynchronizationListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        String parameters = JsonStringRequest.getParameters(params);

        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, ApiUtils.USER_GAME_API_URL + parameters, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> {
                        if (listener != null) {
                            listener.onSynchronizationSucceeded();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e(Tags.STORED_GAMES, String.format(Locale.getDefault(), "Error %d while canceling the scheduled game", error.networkResponse.statusCode));
                        }
                        if (listener != null) {
                            listener.onSynchronizationFailed();
                        }
                    }
            );
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private byte[] gameDescriptionToByteArray(ApiGameDescription gameDescription) throws JsonParseException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(gameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE, writer);
        writer.close();

        outputStream.close();

        return outputStream.toByteArray();
    }

    // Read game descriptions

    private List<ApiGameDescription> readGameDescriptionList(String json) {
        Log.i(Tags.STORED_GAMES, "Read game description list");
        List<ApiGameDescription> gameDescriptionList = new ArrayList<>();

        try {
            gameDescriptionList = JsonIOUtils.GSON.fromJson(json, JsonIOUtils.GAME_DESCRIPTION_LIST_TYPE);
        } catch (JsonParseException e) {
            Log.e(Tags.STORED_GAMES, "Exception while reading game description list", e);
        }

        return gameDescriptionList;
    }

    private void deleteGameOnServer(final long gameDate) {
        if (PrefUtils.isSyncOn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("id", String.valueOf(gameDate));
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, ApiUtils.USER_GAME_API_URL + parameters, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> AppDatabase.getInstance(mContext).syncDao().deleteByItemAndType(SyncEntity.createGameItem(gameDate), SyncEntity.GAME_ENTITY),
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
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, ApiUtils.USER_GAME_API_URL, new byte[0], PrefUtils.getAuthentication(mContext),
                    response -> AppDatabase.getInstance(mContext).syncDao().deleteByType(SyncEntity.GAME_ENTITY),
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

        if (PrefUtils.isSyncOn(mContext)) {
            pushCurrentGameToServer();
        }
    }

    private void pushGameToServer(final StoredGameService storedGameService) {
        if (mBatteryReceiver.canPushGameOnline() && PrefUtils.isSyncOn(mContext) && storedGameService != null && isNotTestGame(storedGameService)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            ApiGame game = (ApiGame) storedGameService;
            final byte[] bytes = writeGame(game).getBytes();

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, ApiUtils.GAMES_API_URL, bytes, authentication,
                    response -> {
                        if (GameStatus.COMPLETED.equals(game.getStatus())) {
                            insertGameIntoDb(game, true);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                            JsonStringRequest stringRequest1 = new JsonStringRequest(Request.Method.POST, ApiUtils.GAMES_API_URL, bytes, authentication,
                                    response -> {
                                        if (GameStatus.COMPLETED.equals(game.getStatus())) {
                                            insertGameIntoDb(game, true);
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            ApiUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void pushCurrentGameToServer() {
        pushGameToServer(mStoredGame);
    }

    private void pushCurrentSetToServer() {
        if (mBatteryReceiver.canPushSetOnline() && PrefUtils.isSyncOn(mContext) && mStoredGame != null && isNotTestGame(mStoredGame)) {
            final Authentication authentication = PrefUtils.getAuthentication(mContext);
            int setIndex = mStoredGame.currentSetIndex();
            ApiSet set = mStoredGame.getSets().get(setIndex);
            final byte[] bytes = writeSet(set).getBytes();

            String url = String.format(Locale.getDefault(), ApiUtils.GAME_SET_API_URL, mStoredGame.getId(), setIndex);
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, url, bytes, authentication,
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

    @Override
    public void syncGames() {
        syncGames(null);
    }

    @Override
    public void syncGames(final DataSynchronizationListener listener) {
        if (PrefUtils.isSyncOn(mContext)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.USER_COMPLETED_GAMES_API_URL, new byte[0],  PrefUtils.getAuthentication(mContext),
                    response -> {
                        List<ApiGameDescription> gameList = readGameDescriptionList(response);
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

    @Override
    public void downloadGame(String id, AsyncGameRequestListener listener) {

    }

    private void syncGames(List<ApiGameDescription> remoteGameList, DataSynchronizationListener listener) {
        String userId = PrefUtils.getAuthentication(mContext).getUserId();
        List<StoredGameService> localGameList = listGames();

        for (StoredGameService localGame : localGameList) {
            StoredGame recordedGame = (StoredGame) localGame;
            if (recordedGame.getUserId().equals(Authentication.VBR_USER_ID)) {
                recordedGame.setUserId(userId);
                insertGameIntoDb(recordedGame);
            }
        }

        for (StoredGameService localGame : localGameList) {
            boolean foundRemoteVersion = false;

            for (ApiGameDescription remoteGame : remoteGameList) {
                if (remoteGame.getGameDate() == localGame.getGameDate()) {
                    foundRemoteVersion = true;
                }
            }

            if (!foundRemoteVersion) {
                if (isSynced(localGame.getGameDate())) {
                    // if the game was synced, then it was deleted from the server and it must be deleted locally
                    deleteGame(localGame.getGameDate());
                } else {
                    // if the game was not synced, then it is missing from the server because sending it must have failed, so send it again
                    pushGameToServer(localGame);
                }
            }
        }

        Queue<ApiGameDescription> missingRemoteGames = new LinkedList<>();

        for (ApiGameDescription remoteGame : remoteGameList) {
            boolean foundLocalVersion = false;

            for (StoredGameService localGame : localGameList) {
                if (localGame.getGameDate() == remoteGame.getGameDate()) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                if (isSynced(remoteGame.getGameDate())) {
                    // if the game was synced, then sending the deletion to the server must have failed, so send the deletion again
                    deleteGameOnServer(remoteGame.getGameDate());
                } else {
                    // if the game was not synced, then it was added on the server and it must be added locally
                    missingRemoteGames.add(remoteGame);
                }
            }
        }

        downloadUserGamesRecursive(missingRemoteGames, listener);
    }

    private void downloadUserGamesRecursive(final Queue<ApiGameDescription> remoteGames, final DataSynchronizationListener listener) {
        if (remoteGames.isEmpty()) {
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            ApiGameDescription remoteGame = remoteGames.poll();
            getUserGame(remoteGame.getGameDate(), new AsyncGameRequestListener() {
                @Override
                public void onGameReceived(StoredGameService storedGameService) {
                    insertGameIntoDb((StoredGame) storedGameService, false);
                    insertSyncIntoDb(storedGameService.getGameDate());
                    downloadUserGamesRecursive(remoteGames, listener);
                }

                @Override
                public void onRecordedGameReceivedFromCode(StoredGameService storedGameService) {}

                @Override
                public void onAvailableGamesReceived(List<ApiGameDescription> gameDescriptionList) {}

                @Override
                public void onNotFound() {
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }

                @Override
                public void onInternalError() {
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }

                @Override
                public void onError() {
                    if (listener != null) {
                        listener.onSynchronizationFailed();
                    }
                }
            });
        }
    }

    private boolean isNotTestGame(StoredGameService storedGameService) {
        return storedGameService.getTeamName(TeamType.HOME).length() > 1 && storedGameService.getTeamName(TeamType.GUEST).length() > 1;
    }

    private boolean isSynced(long gameDate) {
        return AppDatabase.getInstance(mContext).syncDao().countByItemAndType(SyncEntity.createGameItem(gameDate), SyncEntity.GAME_ENTITY) > 0;
    }

    private void insertSyncIntoDb(long gameDate) {
        AppDatabase.getInstance(mContext).syncDao().insert(SyncEntity.createGameSyncEntity(gameDate));
    }
}
