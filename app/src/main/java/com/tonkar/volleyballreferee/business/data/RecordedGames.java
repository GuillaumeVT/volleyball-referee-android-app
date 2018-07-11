package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.game.BaseGame;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.GeneralListener;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class RecordedGames implements RecordedGamesService, GeneralListener, ScoreListener, TeamListener, TimeoutListener, SanctionListener {

    private static final String TAG = "VBR-RecordedGames";
    
    private final Context            mContext;
    private       GameService        mGameService;
    private       RecordedGame       mRecordedGame;
    private final List<RecordedGame> mRecordedGames;
    private final Set<String>        mRecordedLeagues;
    private final Set<String>        mRecordedDivisions;

    public RecordedGames(Context context) {
        mContext = context;
        mRecordedGames = new ArrayList<>();
        mRecordedLeagues = new TreeSet<>();
        mRecordedDivisions = new TreeSet<>();
    }

    @Override
    public void connectGameRecorder() {
        Log.i(TAG, "Connect the game recorder");
        if (hasSetupGame()) {
            deleteSetupGame();
        }

        mGameService = ServicesProvider.getInstance().getGameService();
        mGameService.addGeneralListener(this);
        mGameService.addScoreListener(this);
        mGameService.addTeamListener(this);
        mGameService.addTimeoutListener(this);
        mGameService.addSanctionListener(this);

        createCurrentGame();
        saveCurrentGame();
        pushCurrentGameOnline();
    }

    @Override
    public void disconnectGameRecorder(boolean exiting) {
        Log.i(TAG, "Disconnect the game recorder");
        if (exiting) {
            saveCurrentGame();
            deleteCurrentGameOnline();
        }
        mGameService.removeGeneralListener(this);
        mGameService.removeScoreListener(this);
        mGameService.removeTeamListener(this);
        mGameService.removeTimeoutListener(this);
        mGameService.removeSanctionListener(this);
    }

    @Override
    public void loadRecordedGames() {
        readRecordedGames();
        collectLeaguesAndDivisions();
        syncGamesOnline();
    }

    private void collectLeaguesAndDivisions() {
        mRecordedLeagues.clear();
        mRecordedDivisions.clear();
        for (RecordedGame recordedGame : mRecordedGames) {
            recordedGame.setRefereeName(PrefUtils.getPrefRefereeName(mContext));
            if (recordedGame.getGameSchedule() == 0L) {
                recordedGame.setGameSchedule(recordedGame.getGameDate());
            }
            if (!recordedGame.getLeagueName().isEmpty()) {
                mRecordedLeagues.add(recordedGame.getLeagueName());
            }
            if (!recordedGame.getDivisionName().isEmpty()) {
                mRecordedDivisions.add(recordedGame.getDivisionName());
            }
        }
    }

    @Override
    public List<RecordedGameService> getRecordedGameServiceList() {
        return new ArrayList<RecordedGameService>(mRecordedGames);
    }

    @Override
    public RecordedGameService getRecordedGameService(long gameDate) {
        RecordedGame matching = null;

        for (RecordedGame recordedGame : mRecordedGames) {
            if (recordedGame.getGameDate() == gameDate) {
                matching = recordedGame;
            }
        }

        return matching;
    }

    @Override
    public Set<String> getRecordedLeagues() {
        return mRecordedLeagues;
    }

    @Override
    public Set<String> getRecordedDivisions() {
        return mRecordedDivisions;
    }

    @Override
    public void deleteRecordedGame(long gameDate) {
        for (Iterator<RecordedGame> iterator = mRecordedGames.iterator(); iterator.hasNext();) {
            RecordedGame recordedGame = iterator.next();
            if (recordedGame.getGameDate() == gameDate) {
                iterator.remove();
                deleteGameOnline(recordedGame.getGameDate());
            }
        }
        writeRecordedGames();
    }

    @Override
    public void deleteAllRecordedGames() {
        mRecordedGames.clear();
        deleteAllGamesOnline();
        writeRecordedGames();
    }

    @Override
    public boolean hasCurrentGame() {
        File currentGameFile = mContext.getFileStreamPath(RecordedGamesService.CURRENT_GAME_FILE);
        return currentGameFile != null && currentGameFile.exists();
    }

    @Override
    public GameService loadCurrentGame() {
        GameService gameService = readCurrentGame(CURRENT_GAME_FILE);

        if (gameService == null) {
            int attempts = 0;
            while (attempts < 5 && gameService == null) {
                gameService = readCurrentGame(CURRENT_GAME_FILE);
                attempts++;
            }
        }

        return gameService;
    }

    @Override
    public synchronized void saveCurrentGame() {
        updateCurrentGame();
        if (!mGameService.isMatchCompleted()) {
            writeCurrentGame(CURRENT_GAME_FILE, mGameService);

            int attempts = 0;
            while (attempts < 5 && readCurrentGame(CURRENT_GAME_FILE) == null) {
                writeCurrentGame(CURRENT_GAME_FILE, mGameService);
                attempts++;
            }
        }
    }

    @Override
    public void deleteCurrentGame() {
        Log.d(TAG, String.format("Delete serialized game in %s", CURRENT_GAME_FILE));
        deleteCurrentGameOnline();
        mContext.deleteFile(CURRENT_GAME_FILE);
        mRecordedGame = null;
    }

    @Override
    public boolean hasSetupGame() {
        File setupGameFile = mContext.getFileStreamPath(RecordedGamesService.SETUP_GAME_FILE);
        return setupGameFile != null && setupGameFile.exists();
    }

    @Override
    public GameService loadSetupGame() {
        return readCurrentGame(SETUP_GAME_FILE);
    }

    @Override
    public void saveSetupGame(GameService gameService) {
        writeCurrentGame(SETUP_GAME_FILE, gameService);
    }

    @Override
    public void deleteSetupGame() {
        Log.d(TAG, String.format("Delete serialized setup game in %s", SETUP_GAME_FILE));
        mContext.deleteFile(SETUP_GAME_FILE);
    }

    @Override
    public boolean isGameIndexed(long gameDate) {
        boolean indexed = false;
        RecordedGameService recordedGameService = getRecordedGameService(gameDate);

        if (recordedGameService != null) {
            indexed = recordedGameService.isIndexed();
        }
        return indexed;
    }

    @Override
    public void toggleGameIndexed(long gameDate) {
        RecordedGameService recordedGameService = getRecordedGameService(gameDate);

        if (recordedGameService != null) {
            recordedGameService.setIndexed(!recordedGameService.isIndexed());
            pushGameOnline(recordedGameService);
            writeRecordedGames();
        }
    }

    private void pushGameOnline(final RecordedGameService recordedGameService) {
        if (PrefUtils.isPrefDataSyncEnabled(mContext) && recordedGameService != null) {
            RecordedGame recordedGame = (RecordedGame) recordedGameService;
            try {
                final byte[] bytes = recordedGameToByteArray(recordedGame);

                String url = String.format(Locale.getDefault(), WebUtils.GAME_API_URL, recordedGameService.getGameDate());
                JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, url, bytes,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {}
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    Log.e(TAG, String.format(Locale.getDefault(), "Error %d while uploading game", error.networkResponse.statusCode));
                                }
                            }
                        }
                );
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
            } catch (JsonParseException | IOException e) {
                Log.e(TAG, "Exception while writing game", e);
            }
        }
    }

    private void pushCurrentGameOnline() {
        pushGameOnline(mRecordedGame);
    }

    private void pushCurrentSetOnline() {
        if (PrefUtils.isSyncOn(mContext) && mRecordedGame != null) {
            int setIndex = mRecordedGame.currentSetIndex();
            RecordedSet recordedSet = mRecordedGame.getSets().get(setIndex);

            try {
                final byte[] bytes = recordedSetToByteArray(recordedSet);

                String url = String.format(Locale.getDefault(), WebUtils.SET_API_URL, mRecordedGame.getGameDate(), setIndex);
                JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, url, bytes,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {}
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    Log.e(TAG, String.format(Locale.getDefault(), "Error %d while uploading set", error.networkResponse.statusCode));
                                    if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                        pushCurrentGameOnline();
                                    }
                                }
                            }
                        }
                );
                WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
            } catch (JsonParseException | IOException e) {
                Log.e(TAG, "Exception while writing game", e);
            }
        }
    }

    private void deleteCurrentGameOnline() {
        if (PrefUtils.isPrefDataSyncEnabled(mContext)) {
            // The match is not loaded in memory => need to read it to retrieve the date
            if (mRecordedGame == null) {
                GameService gameService = loadCurrentGame();
                if (gameService != null) {
                    String url = String.format(Locale.getDefault(), WebUtils.GAME_API_URL, gameService.getGameDate());
                    JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, url, new byte[0],
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {}
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse != null) {
                                        Log.e(TAG, String.format(Locale.getDefault(), "Error %d while deleting game", error.networkResponse.statusCode));
                                    }
                                }
                            }
                    );
                    WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
                }
            } else { // The match is loaded in memory
                if (!mRecordedGame.isMatchCompleted()) {
                    String url = String.format(Locale.getDefault(), WebUtils.GAME_API_URL, mRecordedGame.getGameDate());
                    JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, url, new byte[0],
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {}
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse != null) {
                                        Log.e(TAG, String.format(Locale.getDefault(), "Error %d while deleting game", error.networkResponse.statusCode));
                                    }
                                }
                            }
                    );
                    WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
                }
            }
        }
    }

    @Override
    public void onMatchCompleted(TeamType winner) {
        if (mRecordedGame != null) {
            mRecordedGames.add(mRecordedGame);
        }
        writeRecordedGames();
        pushCurrentGameOnline();
        deleteCurrentGame();
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        saveCurrentGame();
        pushCurrentSetOnline();
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType) {}

    @Override
    public void onSetStarted() {
        saveCurrentGame();
        pushCurrentGameOnline();
    }

    @Override
    public void onSetCompleted() {
        saveCurrentGame();
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        saveCurrentGame();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        saveCurrentGame();
        pushCurrentSetOnline();
    }

    @Override
    public void onTeamRotated(TeamType teamType) {
        saveCurrentGame();
        pushCurrentSetOnline();
    }

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {}

    @Override
    public void onTimeout(TeamType teamType, int duration) {
        saveCurrentGame();
        pushCurrentGameOnline();
    }

    @Override
    public void onTechnicalTimeout(int duration) {
        saveCurrentGame();
        pushCurrentGameOnline();
    }

    @Override
    public void onGameInterval(int duration) {}

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        saveCurrentGame();
        pushCurrentGameOnline();
    }

    private void createCurrentGame() {
        String userId = PrefUtils.getUserId(mContext);

        mRecordedGame = new RecordedGame();
        mRecordedGame.setUserId(userId);
        mRecordedGame.setGameType(mGameService.getGameType());
        mRecordedGame.setGameDate(mGameService.getGameDate());
        mRecordedGame.setGameSchedule(mGameService.getGameSchedule());
        mRecordedGame.setGenderType(mGameService.getGenderType());
        mRecordedGame.setUsageType(mGameService.getUsageType());
        mRecordedGame.setRefereeName(PrefUtils.getPrefRefereeName(mContext));
        mRecordedGame.setLeagueName(mGameService.getLeagueName());
        mRecordedGame.setDivisionName(mGameService.getDivisionName());

        RecordedTeam homeTeam = mRecordedGame.getTeam(TeamType.HOME);
        homeTeam.setName(mGameService.getTeamName(TeamType.HOME));
        homeTeam.setColor(mGameService.getTeamColor(TeamType.HOME));
        homeTeam.setGenderType(mGameService.getGenderType(TeamType.HOME));
        homeTeam.setUserId(userId);
        homeTeam.setGameType(mRecordedGame.getGameType());

        RecordedTeam guestTeam = mRecordedGame.getTeam(TeamType.GUEST);
        guestTeam.setName(mGameService.getTeamName(TeamType.GUEST));
        guestTeam.setColor(mGameService.getTeamColor(TeamType.GUEST));
        guestTeam.setGenderType(mGameService.getGenderType(TeamType.GUEST));
        guestTeam.setUserId(userId);
        guestTeam.setGameType(mRecordedGame.getGameType());

        homeTeam.setLiberoColor(mGameService.getLiberoColor(TeamType.HOME));
        guestTeam.setLiberoColor(mGameService.getLiberoColor(TeamType.GUEST));

        for (int number : mGameService.getPlayers(TeamType.HOME)) {
            if (mGameService.isLibero(TeamType.HOME, number)) {
                homeTeam.getLiberos().add(number);
            } else {
                homeTeam.getPlayers().add(number);
            }
        }
        for (int number : mGameService.getPlayers(TeamType.GUEST)) {
            if (mGameService.isLibero(TeamType.GUEST, number)) {
                guestTeam.getLiberos().add(number);
            } else {
                guestTeam.getPlayers().add(number);
            }
        }

        homeTeam.setCaptain(mGameService.getCaptain(TeamType.HOME));
        guestTeam.setCaptain(mGameService.getCaptain(TeamType.GUEST));

        mRecordedGame.setRules(mGameService.getRules());
        mRecordedGame.getRules().setUserId(userId);

        updateCurrentGame();
    }

    private void updateCurrentGame() {
        if (mRecordedGame != null) {
            mRecordedGame.setRefereeName(PrefUtils.getPrefRefereeName(mContext));
            mRecordedGame.setMatchStatus(mGameService.isMatchCompleted() ? GameStatus.COMPLETED : GameStatus.LIVE);
            mRecordedGame.setIndexed(mGameService.isIndexed());
            mRecordedGame.setSets(TeamType.HOME, mGameService.getSets(TeamType.HOME));
            mRecordedGame.setSets(TeamType.GUEST, mGameService.getSets(TeamType.GUEST));

            mRecordedGame.getSets().clear();

            for (int setIndex = 0 ; setIndex < mGameService.getNumberOfSets(); setIndex++) {
                RecordedSet set = new RecordedSet();

                set.setDuration(mGameService.getSetDuration(setIndex));
                set.getPointsLadder().addAll(mGameService.getPointsLadder(setIndex));
                set.setServingTeam(mGameService.getServingTeam(setIndex));
                set.setFirstServingTeam(mGameService.getFirstServingTeam(setIndex));

                set.setPoints(TeamType.HOME, mGameService.getPoints(TeamType.HOME, setIndex));
                set.setTimeouts(TeamType.HOME, mGameService.getRemainingTimeouts(TeamType.HOME, setIndex));
                for (int number : mGameService.getPlayersOnCourt(TeamType.HOME, setIndex)) {
                    RecordedPlayer player = new RecordedPlayer();
                    player.setNumber(number);
                    player.setPositionType(mGameService.getPlayerPosition(TeamType.HOME, number, setIndex));
                    set.getCurrentPlayers(TeamType.HOME).add(player);
                }
                for (Timeout timeout : mGameService.getCalledTimeouts(TeamType.HOME, setIndex)) {
                    Timeout to = new Timeout(timeout.getHomeTeamPoints(), timeout.getGuestTeamPoints());
                    set.getCalledTimeouts(TeamType.HOME).add(to);
                }

                set.setPoints(TeamType.GUEST, mGameService.getPoints(TeamType.GUEST, setIndex));
                set.setTimeouts(TeamType.GUEST, mGameService.getRemainingTimeouts(TeamType.GUEST, setIndex));
                for (int number : mGameService.getPlayersOnCourt(TeamType.GUEST, setIndex)) {
                    RecordedPlayer player = new RecordedPlayer();
                    player.setNumber(number);
                    player.setPositionType(mGameService.getPlayerPosition(TeamType.GUEST, number, setIndex));
                    set.getCurrentPlayers(TeamType.GUEST).add(player);
                }
                for (Timeout timeout : mGameService.getCalledTimeouts(TeamType.GUEST, setIndex)) {
                    Timeout to = new Timeout(timeout.getHomeTeamPoints(), timeout.getGuestTeamPoints());
                    set.getCalledTimeouts(TeamType.GUEST).add(to);
                }

                if (GameType.TIME.equals(mRecordedGame.getGameType()) && mGameService instanceof TimeBasedGameService) {
                    TimeBasedGameService timeBasedGameService = (TimeBasedGameService) mGameService;
                    set.setRemainingTime(timeBasedGameService.getRemainingTime(setIndex));
                }

                if (mGameService instanceof IndoorTeamService) {
                    IndoorTeamService indoorTeamService = (IndoorTeamService) mGameService;

                    for (int number : indoorTeamService.getPlayersInStartingLineup(TeamType.HOME, setIndex)) {
                        RecordedPlayer player = new RecordedPlayer();
                        player.setNumber(number);
                        player.setPositionType(indoorTeamService.getPlayerPositionInStartingLineup(TeamType.HOME, number, setIndex));
                        set.getStartingPlayers(TeamType.HOME).add(player);
                    }

                    for (Substitution substitution : indoorTeamService.getSubstitutions(TeamType.HOME, setIndex)) {
                        Substitution sub = new Substitution(substitution.getPlayerIn(), substitution.getPlayerOut(), substitution.getHomeTeamPoints(), substitution.getGuestTeamPoints());
                        set.getSubstitutions(TeamType.HOME).add(sub);
                    }

                    for (int number : indoorTeamService.getPlayersInStartingLineup(TeamType.GUEST, setIndex)) {
                        RecordedPlayer player = new RecordedPlayer();
                        player.setNumber(number);
                        player.setPositionType(indoorTeamService.getPlayerPositionInStartingLineup(TeamType.GUEST, number, setIndex));
                        set.getStartingPlayers(TeamType.GUEST).add(player);
                    }

                    for (Substitution substitution : indoorTeamService.getSubstitutions(TeamType.GUEST, setIndex)) {
                        Substitution sub = new Substitution(substitution.getPlayerIn(), substitution.getPlayerOut(), substitution.getHomeTeamPoints(), substitution.getGuestTeamPoints());
                        set.getSubstitutions(TeamType.GUEST).add(sub);
                    }

                    set.setActingCaptain(TeamType.HOME, indoorTeamService.getActingCaptain(TeamType.HOME, setIndex));
                    set.setActingCaptain(TeamType.GUEST, indoorTeamService.getActingCaptain(TeamType.GUEST, setIndex));
                }

                mRecordedGame.getSets().add(set);
            }

            mRecordedGame.getGivenSanctions(TeamType.HOME).clear();

            for (Sanction sanction : mGameService.getGivenSanctions(TeamType.HOME)) {
                Sanction sanct = new Sanction(sanction.getPlayer(), sanction.getSanctionType(), sanction.getSetIndex(), sanction.getHomeTeamPoints(), sanction.getGuestTeamPoints());
                mRecordedGame.getGivenSanctions(TeamType.HOME).add(sanct);
            }

            mRecordedGame.getGivenSanctions(TeamType.GUEST).clear();

            for (Sanction sanction : mGameService.getGivenSanctions(TeamType.GUEST)) {
                Sanction sanct = new Sanction(sanction.getPlayer(), sanction.getSanctionType(), sanction.getSetIndex(), sanction.getHomeTeamPoints(), sanction.getGuestTeamPoints());
                mRecordedGame.getGivenSanctions(TeamType.GUEST).add(sanct);
            }
        }
    }

    // Read current game

    private GameService readCurrentGame(String fileName) {
        Log.i(TAG, String.format("Read current game from %s", fileName));
        BaseGame game = null;

        try {
            FileInputStream inputStream = mContext.openFileInput(fileName);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            try {
                game = JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.CURRENT_GAME_TYPE);
            } finally {
                reader.close();
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, String.format("%s current game file does not yet exist", fileName));
        } catch (JsonParseException | IOException e) {
            Log.e(TAG, "Exception while reading current game", e);
        }

        return game;
    }

    // Write current game

    private void writeCurrentGame(String fileName, GameService gameService) {
        Log.i(TAG, String.format("Write current game into %s", fileName));
        try {
            FileOutputStream outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            JsonIOUtils.GSON.toJson(gameService, JsonIOUtils.CURRENT_GAME_TYPE, writer);
            writer.close();
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e(TAG, "Exception while writing current game", e);
        }
    }

    // Read recorded games

    private void readRecordedGames() {
        Log.i(TAG, String.format("Read recorded games from %s", RECORDED_GAMES_FILE));

        try {
            FileInputStream inputStream = mContext.openFileInput(RECORDED_GAMES_FILE);
            mRecordedGames.clear();
            mRecordedGames.addAll(readRecordedGamesStream(inputStream));
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, String.format("%s recorded games file does not yet exist", RECORDED_GAMES_FILE));
        } catch (JsonParseException | IOException e) {
            Log.e(TAG, "Exception while reading games", e);
        }
    }

    public static List<RecordedGame> readRecordedGamesStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RECORDED_GAME_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    public static RecordedGame byteArrayToRecordedGame(byte[] bytes) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        try {
            return JsonIOUtils.GSON.fromJson(reader, JsonIOUtils.RECORDED_GAME_TYPE);
        } finally {
            reader.close();
        }
    }

    private RecordedGame readRecordedGame(String json) {
        Log.i(TAG, "Read recorded game");
        RecordedGame recordedGame = null;

        try {
            recordedGame = JsonIOUtils.GSON.fromJson(json, JsonIOUtils.RECORDED_GAME_TYPE);
        } catch (JsonParseException e) {
            Log.e(TAG, "Exception while reading game", e);
        }

        return recordedGame;
    }

    // Write recorded games

    private void writeRecordedGames() {
        Log.i(TAG, String.format("Write recorded games into %s", RECORDED_GAMES_FILE));
        try {
            FileOutputStream outputStream = mContext.openFileOutput(RECORDED_GAMES_FILE, Context.MODE_PRIVATE);
            writeRecordedGamesStream(outputStream, mRecordedGames);
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e(TAG, "Exception while writing games", e);
        }
    }

    public static void writeRecordedGamesStream(OutputStream outputStream, List<RecordedGame> recordedGames) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(recordedGames, JsonIOUtils.RECORDED_GAME_LIST_TYPE, writer);
        writer.close();
    }

    public static byte[] recordedGameToByteArray(RecordedGame recordedGame) throws JsonParseException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        JsonIOUtils.GSON.toJson(recordedGame, JsonIOUtils.RECORDED_GAME_TYPE, writer);
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
        String url = String.format(Locale.getDefault(), WebUtils.GAME_CODE_API_URL, code);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, url, new byte[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        RecordedGame recordedGame = readRecordedGame(response);

                        if (recordedGame == null) {
                            Log.e(TAG, "Failed to deserialize a game from code or to notify the listener");
                            listener.onInternalError();
                        } else {
                            listener.onRecordedGameReceivedFromCode(recordedGame);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            Log.e(TAG, String.format(Locale.getDefault(), "Error %d getting a game from code", error.networkResponse.statusCode));
                            if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                listener.onNotFound();
                            } else {
                                listener.onError();
                            }
                        } else {
                            listener.onError();
                        }
                    }
                }
        );
        WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
    }

    @Override
    public void getUserGame(final String userId, final long id, final AsyncGameRequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("id", String.valueOf(id));
        String parameters = JsonStringRequest.getParameters(params);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_GAME_API_URL + parameters, new byte[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        RecordedGame recordedGame = readRecordedGame(response);

                        if (recordedGame == null) {
                            Log.e(TAG, "Failed to deserialize a user game or to notify the listener");
                            listener.onInternalError();
                        } else {
                            if (listener != null) {
                                listener.onUserGameReceived(recordedGame);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            Log.e(TAG, String.format(Locale.getDefault(), "Error %d getting a user game", error.networkResponse.statusCode));
                            if (HttpURLConnection.HTTP_NOT_FOUND == error.networkResponse.statusCode) {
                                listener.onNotFound();
                            } else {
                                listener.onError();
                            }
                        } else {
                            listener.onError();
                        }
                    }
                }
        );
        WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
    }

    @Override
    public void getUserScheduledGames(final String userId, final AsyncGameRequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        String parameters = JsonStringRequest.getParameters(params);

        JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_SCHEDULED_GAMES_API_URL + parameters, new byte[0],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<GameDescription> gameDescriptionList = readGameDescriptionList(response);

                        if (gameDescriptionList == null) {
                            Log.e(TAG, "Failed to deserialize a user scheduled game list or to notify the listener");
                            listener.onInternalError();
                        } else {
                            if (listener != null) {
                                listener.onUserGameListReceived(gameDescriptionList);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            Log.e(TAG, String.format(Locale.getDefault(), "Error %d getting a user scheduled game list", error.networkResponse.statusCode));
                        }
                        listener.onError();
                    }
                }
        );
        WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
    }

    // Read game descriptions

    private List<GameDescription> readGameDescriptionList(String json) {
        Log.i(TAG, "Read game description list");
        List<GameDescription> gameDescriptionList = new ArrayList<>();

        try {
            gameDescriptionList = JsonIOUtils.GSON.fromJson(json, JsonIOUtils.GAME_DESCRIPTION_LIST_TYPE);
        } catch (JsonParseException e) {
            Log.e(TAG, "Exception while reading game description list", e);
        }

        return gameDescriptionList;
    }

    private void deleteGameOnline(long gameDate) {
        if (PrefUtils.isSyncOn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("userId", PrefUtils.getUserId(mContext));
            params.put("id", String.valueOf(gameDate));
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_GAME_API_URL + parameters, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(TAG, String.format(Locale.getDefault(), "Error %d while deleting game", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    private void deleteAllGamesOnline() {
        if (PrefUtils.isSyncOn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("userId", PrefUtils.getUserId(mContext));
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, WebUtils.USER_GAME_API_URL + parameters, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(TAG, String.format(Locale.getDefault(), "Error %d while all deleting games", error.networkResponse.statusCode));
                            }
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(mContext).add(stringRequest);
        }
    }

    @Override
    public void onMatchIndexed(boolean indexed) {
        saveCurrentGame();

        if (PrefUtils.isPrefDataSyncEnabled(mContext)) {
            if (indexed) {
                Log.d(TAG, "Making the game public");
                pushCurrentGameOnline();
            } else {
                if (PrefUtils.canSync(mContext)) {
                    Log.d(TAG, "Making the game private");
                    pushCurrentGameOnline();
                } else {
                    Log.d(TAG, "Deleting the private game");
                    deleteCurrentGameOnline();
                }
            }
        }
    }

    @Override
    public void syncGamesOnline() {
        syncGamesOnline(null);
    }

    @Override
    public void syncGamesOnline(final DataSynchronizationListener listener) {
        if (PrefUtils.isSyncOn(mContext)) {
            Map<String, String> params = new HashMap<>();
            params.put("userId", PrefUtils.getUserId(mContext));
            String parameters = JsonStringRequest.getParameters(params);

            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, WebUtils.USER_COMPLETED_GAMES_API_URL + parameters, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            List<GameDescription> gameList = readGameDescriptionList(response);
                            syncGames(gameList, listener);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(TAG, String.format(Locale.getDefault(), "Error %d while synchronising games", error.networkResponse.statusCode));
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

    private void syncGames(List<GameDescription> remoteGameList, DataSynchronizationListener listener) {
        for (RecordedGame localGame : mRecordedGames) {
            boolean foundRemoteVersion = false;

            for (GameDescription remoteGame : remoteGameList) {
                if (remoteGame.getGameDate() == localGame.getGameDate()) {
                    foundRemoteVersion = true;
                }
            }

            if (!foundRemoteVersion) {
                pushGameOnline(localGame);
            }
        }

        Queue<GameDescription> missingRemoteGames = new LinkedList<>();

        for (GameDescription remoteGame : remoteGameList) {
            boolean foundLocalVersion = false;

            for (RecordedGame localGame : mRecordedGames) {
                if (localGame.getGameDate() == remoteGame.getGameDate()) {
                    foundLocalVersion = true;
                }
            }

            if (!foundLocalVersion) {
                missingRemoteGames.add(remoteGame);
            }
        }

        downloadUserGamesRecursive(missingRemoteGames, listener);
    }

    private void downloadUserGamesRecursive(final Queue<GameDescription> remoteGames, final DataSynchronizationListener listener) {
        if (remoteGames.isEmpty()) {
            writeRecordedGames();
            collectLeaguesAndDivisions();
            if (listener != null) {
                listener.onSynchronizationSucceeded();
            }
        } else {
            GameDescription remoteGame = remoteGames.poll();
            getUserGame(PrefUtils.getUserId(mContext), remoteGame.getGameDate(), new AsyncGameRequestListener() {
                @Override
                public void onUserGameReceived(RecordedGameService recordedGameService) {
                    mRecordedGames.add((RecordedGame) recordedGameService);
                    downloadUserGamesRecursive(remoteGames, listener);
                }

                @Override
                public void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService) {}

                @Override
                public void onUserGameListReceived(List<GameDescription> gameDescriptionList) {}

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
}
