package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.Timeout;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.WebGamesService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class RecordedGames implements RecordedGamesService, ScoreListener, TeamListener, TimeoutListener {

    private final Context            mContext;
    private       GameService        mGameService;
    private       RecordedGame       mRecordedGame;
    private final List<RecordedGame> mRecordedGames;
    private final Set<String>        mRecordedLeagues;
    private       boolean            mOnlineRecordingEnabled;

    public RecordedGames(Context context) {
        mContext = context;
        mRecordedGames = new ArrayList<>();
        mRecordedLeagues = new TreeSet<>();
        mOnlineRecordingEnabled = true;
    }

    @Override
    public void connectGameRecorder() {
        Log.i("VBR-Data", "Connect the game recorder");
        if (hasSetupGame()) {
            deleteSetupGame();
        }

        mGameService = ServicesProvider.getInstance().getGameService();
        mGameService.addScoreListener(this);
        mGameService.addTeamListener(this);
        mGameService.addTimeoutListener(this);

        createRecordedGame();
        saveCurrentGame();

        mOnlineRecordingEnabled = PrefUtils.isPrefOnlineRecordingEnabled(mContext);
        uploadCurrentGameOnline();
    }

    @Override
    public void disconnectGameRecorder(boolean exiting) {
        Log.i("VBR-Data", "Disconnect the game recorder");
        if (exiting) {
            saveCurrentGame();
            deleteCurrentGameOnline();
        }
        mGameService.removeScoreListener(this);
        mGameService.removeTeamListener(this);
        mGameService.removeTimeoutListener(this);
    }

    @Override
    public void loadRecordedGames() {
        mRecordedGames.clear();
        mRecordedLeagues.clear();
        mRecordedGames.addAll(JsonIOUtils.readRecordedGames(mContext, RECORDED_GAMES_FILE));
        assessAreRecordedOnline();
        for (RecordedGame recordedGame : mRecordedGames) {
            mRecordedLeagues.add(recordedGame.getLeagueName());
        }
    }

    @Override
    public List<RecordedGameService> getRecordedGameServiceList() {
        List<RecordedGameService> list = new ArrayList<>();
        list.addAll(mRecordedGames);
        return list;
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
    public void deleteRecordedGame(long gameDate) {
        for (Iterator<RecordedGame> iterator = mRecordedGames.iterator(); iterator.hasNext();) {
            RecordedGame recordedGame = iterator.next();
            if (recordedGame.getGameDate() == gameDate) {
                iterator.remove();
            }
        }
        JsonIOUtils.writeRecordedGames(mContext, RECORDED_GAMES_FILE, mRecordedGames);
    }

    @Override
    public void deleteAllRecordedGames() {
        mRecordedGames.clear();
        JsonIOUtils.writeRecordedGames(mContext, RECORDED_GAMES_FILE, mRecordedGames);
    }

    @Override
    public boolean hasCurrentGame() {
        File currentGameFile = mContext.getFileStreamPath(RecordedGamesService.CURRENT_GAME_FILE);
        return currentGameFile != null && currentGameFile.exists();
    }

    @Override
    public GameService loadCurrentGame() {
        return SerializedGameReader.readGame(mContext, CURRENT_GAME_FILE);
    }

    @Override
    public void saveCurrentGame() {
        updateRecordedGame();
        if (!mGameService.isMatchCompleted()) {
            SerializedGameWriter.writeGame(mContext, CURRENT_GAME_FILE, mGameService);
        }
    }

    @Override
    public void deleteCurrentGame() {
        Log.d("VBR-Data", String.format("Delete serialized game in %s", CURRENT_GAME_FILE));
        mContext.deleteFile(CURRENT_GAME_FILE);
        deleteCurrentGameOnline();
        mRecordedGame = null;
    }

    @Override
    public boolean hasSetupGame() {
        File setupGameFile = mContext.getFileStreamPath(RecordedGamesService.SETUP_GAME_FILE);
        return setupGameFile != null && setupGameFile.exists();
    }

    @Override
    public GameService loadSetupGame() {
        return SerializedGameReader.readGame(mContext, SETUP_GAME_FILE);
    }

    @Override
    public void saveSetupGame(GameService gameService) {
        SerializedGameWriter.writeGame(mContext, SETUP_GAME_FILE, gameService);
    }

    @Override
    public void deleteSetupGame() {
        Log.d("VBR-Data", String.format("Delete serialized setup game in %s", SETUP_GAME_FILE));
        mContext.deleteFile(SETUP_GAME_FILE);
    }

    @Override
    public boolean isOnlineRecordingEnabled() {
        return mOnlineRecordingEnabled;
    }

    @Override
    public void toggleOnlineRecording() {
        mOnlineRecordingEnabled = !mOnlineRecordingEnabled;

        if (mOnlineRecordingEnabled) {
            Log.d("VBR-Data", "Enable the online recording");
            uploadCurrentGameOnline();
        } else {
            Log.d("VBR-Data", "Disable the online recording");
            deleteCurrentGameOnline();
        }
    }

    @Override
    public void assessAreRecordedOnline() {
        if (PrefUtils.isPrefOnlineRecordingEnabled(mContext)) {
            RequestQueue queue = Volley.newRequestQueue(mContext);

            for (final RecordedGame recordedGame : mRecordedGames) {
                String url = String.format(Locale.getDefault(), WebGamesService.GAME_API_URL, recordedGame.getGameDate());
                BooleanRequest booleanRequest = new BooleanRequest(Request.Method.GET, url,
                        new Response.Listener<Boolean>() {
                            @Override
                            public void onResponse(Boolean response) {
                                recordedGame.setRecordedOnline(response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        recordedGame.setRecordedOnline(false);
                    }
                }
                );
                queue.add(booleanRequest);
            }
        }
    }

    @Override
    public boolean isGameRecordedOnline(long gameDate) {
        boolean result = false;
        RecordedGameService recordedGameService = getRecordedGameService(gameDate);
        if (recordedGameService != null) {
            result = recordedGameService.isRecordedOnline();
        }
        return result;
    }

    @Override
    public void uploadRecordedGameOnline(long gameDate) {
        RecordedGameService recordedGameService = getRecordedGameService(gameDate);
        if (recordedGameService != null) {
            uploadRecordedGameOnline(recordedGameService, true, true);
        }
    }

    private void uploadRecordedGameOnline(final RecordedGameService recordedGameService, final boolean fromPref, final boolean notify) {
        boolean onlineRecordingEnabled;
        if (fromPref) {
            onlineRecordingEnabled = PrefUtils.isPrefOnlineRecordingEnabled(mContext);
        } else {
            onlineRecordingEnabled = mOnlineRecordingEnabled;
        }

        if (onlineRecordingEnabled && recordedGameService != null) {
            RecordedGame recordedGame = (RecordedGame) recordedGameService;
            try {
                final byte[] bytes = JsonIOUtils.recordedGameToByteArray(recordedGame);

                RequestQueue queue = Volley.newRequestQueue(mContext);
                String url = String.format(Locale.getDefault(), WebGamesService.GAME_API_URL, recordedGameService.getGameDate());
                JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, url, bytes,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                recordedGameService.setRecordedOnline(true);
                                if (notify) {
                                    Toast.makeText(mContext, mContext.getResources().getString(R.string.upload_success_message), Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                recordedGameService.setRecordedOnline(false);
                                Log.e("VBR-Data", "Exception while uploading game", error);
                                if (notify) {
                                    Toast.makeText(mContext, mContext.getResources().getString(R.string.upload_error_message), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                );
                queue.add(stringRequest);
            } catch (IOException e) {
                Log.e("VBR-Data", "Exception while writing game", e);
            }
        }
    }

    private void uploadCurrentGameOnline() {
        uploadRecordedGameOnline(mRecordedGame, false, false);
    }

    private void uploadCurrentSetOnline() {
        if (mOnlineRecordingEnabled && mRecordedGame != null) {
            int setIndex = mRecordedGame.currentSetIndex();
            RecordedSet recordedSet = mRecordedGame.getSets().get(setIndex);

            try {
                final byte[] bytes = JsonIOUtils.recordedSetToByteArray(recordedSet);

                RequestQueue queue = Volley.newRequestQueue(mContext);
                String url = String.format(Locale.getDefault(), WebGamesService.SET_API_URL, mRecordedGame.getGameDate(), setIndex);
                JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.PUT, url, bytes,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("VBR-Data", "Exception while uploading set", error);
                            }
                        }
                );
                queue.add(stringRequest);
            } catch (IOException e) {
                Log.e("VBR-Data", "Exception while writing game", e);
            }
        }
    }

    private void deleteCurrentGameOnline() {
        if (PrefUtils.isPrefOnlineRecordingEnabled(mContext) && mRecordedGame != null && !mRecordedGame.isMatchCompleted()) {
            RequestQueue queue = Volley.newRequestQueue(mContext);
            String url = String.format(Locale.getDefault(), WebGamesService.GAME_API_URL, mRecordedGame.getGameDate());
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.DELETE, url, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mRecordedGame.setRecordedOnline(false);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VBR-Data", "Exception while deleting game", error);
                        }
                    }
            );
            queue.add(stringRequest);
        }
    }

    @Override
    public void onMatchCompleted(TeamType winner) {
        if (mRecordedGame != null) {
            mRecordedGames.add(mRecordedGame);
        }
        JsonIOUtils.writeRecordedGames(mContext, RECORDED_GAMES_FILE, mRecordedGames);
        uploadCurrentGameOnline();
        deleteCurrentGame();
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        saveCurrentGame();
        uploadCurrentSetOnline();
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType) {}

    @Override
    public void onSetStarted() {
        saveCurrentGame();
        uploadCurrentGameOnline();
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
        uploadCurrentSetOnline();
    }

    @Override
    public void onTeamRotated(TeamType teamType) {
        saveCurrentGame();
        uploadCurrentSetOnline();
    }

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {
        saveCurrentGame();
        uploadCurrentSetOnline();
    }

    @Override
    public void onTimeout(TeamType teamType, int duration) {}

    @Override
    public void onTechnicalTimeout(int duration) {}

    @Override
    public void onGameInterval(int duration) {}

    private void createRecordedGame() {
        mRecordedGame = new RecordedGame();
        mRecordedGame.setGameType(mGameService.getGameType());
        mRecordedGame.setGameDate(mGameService.getGameDate());
        mRecordedGame.setGenderType(mGameService.getGenderType());
        mRecordedGame.setUsageType(mGameService.getUsageType());
        mRecordedGame.setLeagueName(mGameService.getLeagueName());

        RecordedTeam homeTeam = mRecordedGame.getTeam(TeamType.HOME);
        homeTeam.setName(mGameService.getTeamName(TeamType.HOME));
        homeTeam.setColor(mGameService.getTeamColor(TeamType.HOME));
        homeTeam.setGenderType(mGameService.getGenderType(TeamType.HOME));

        RecordedTeam guestTeam = mRecordedGame.getTeam(TeamType.GUEST);
        guestTeam.setName(mGameService.getTeamName(TeamType.GUEST));
        guestTeam.setColor(mGameService.getTeamColor(TeamType.GUEST));
        guestTeam.setGenderType(mGameService.getGenderType(TeamType.GUEST));

        if (mGameService instanceof IndoorTeamService) {
            IndoorTeamService indoorTeamService = (IndoorTeamService) mGameService;

            homeTeam.setLiberoColor(indoorTeamService.getLiberoColor(TeamType.HOME));
            guestTeam.setLiberoColor(indoorTeamService.getLiberoColor(TeamType.GUEST));

            for (int number : mGameService.getPlayers(TeamType.HOME)) {
                if (indoorTeamService.isLibero(TeamType.HOME, number)) {
                    homeTeam.getLiberos().add(number);
                } else {
                    homeTeam.getPlayers().add(number);
                }
            }
            for (int number : mGameService.getPlayers(TeamType.GUEST)) {
                if (indoorTeamService.isLibero(TeamType.GUEST, number)) {
                    guestTeam.getLiberos().add(number);
                } else {
                    guestTeam.getPlayers().add(number);
                }
            }

            homeTeam.setCaptain(indoorTeamService.getCaptain(TeamType.HOME));
            guestTeam.setCaptain(indoorTeamService.getCaptain(TeamType.GUEST));
        } else {
            homeTeam.getPlayers().addAll(mGameService.getPlayers(TeamType.HOME));
            guestTeam.getPlayers().addAll(mGameService.getPlayers(TeamType.GUEST));
        }

        updateRecordedGame();
    }

    private void updateRecordedGame() {
        if (mRecordedGame != null) {
            mRecordedGame.setMatchCompleted(mGameService.isMatchCompleted());
            mRecordedGame.setSets(TeamType.HOME, mGameService.getSets(TeamType.HOME));
            mRecordedGame.setSets(TeamType.GUEST, mGameService.getSets(TeamType.GUEST));

            mRecordedGame.getSets().clear();

            for (int setIndex = 0 ; setIndex < mGameService.getNumberOfSets(); setIndex++) {
                RecordedSet set = new RecordedSet();

                set.setDuration(mGameService.getSetDuration(setIndex));
                set.getPointsLadder().addAll(mGameService.getPointsLadder(setIndex));
                set.setServingTeam(mGameService.getServingTeam(setIndex));

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

                if (UsageType.TIME_SCOREBOARD.equals(mRecordedGame.getUsageType()) && mGameService instanceof TimeBasedGameService) {
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
        }
    }

}
