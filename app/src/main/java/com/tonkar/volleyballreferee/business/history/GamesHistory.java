package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.util.Log;

import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.business.game.Game;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.ScoreClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.ScoreService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeoutClient;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.TimeoutService;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamesHistory implements GamesHistoryService, ScoreClient, TeamClient, TimeoutClient, ScoreListener, TeamListener, TimeoutListener {

    private final Context            mContext;
    private       ScoreService       mScoreService;
    private       TeamService        mTeamService;
    private       TimeoutService     mTimeoutService;
    private       RecordedGame       mRecordedGame;
    private final List<RecordedGame> mRecordedGames;

    public GamesHistory(Context context) {
        mContext = context;
        mRecordedGames = new ArrayList<>();
    }

    @Override
    public void setScoreService(ScoreService scoreService) {
        mScoreService = scoreService;
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public void setTimeoutService(TimeoutService timeoutService) {
        mTimeoutService = timeoutService;
    }

    @Override
    public void connectGameRecorder() {
        Log.i("VBR-History", "Connect the game recorder");
        setScoreService(ServicesProvider.getInstance().getScoreService());
        setTeamService(ServicesProvider.getInstance().getTeamService());
        setTimeoutService(ServicesProvider.getInstance().getTimeoutService());
        mScoreService.addScoreListener(this);
        mTeamService.addTeamListener(this);
        mTimeoutService.addTimeoutListener(this);
        createRecordedGame();
        saveCurrentGame();
    }

    @Override
    public void disconnectGameRecorder() {
        Log.i("VBR-History", "Disconnect the game recorder");
        saveCurrentGame();
        mScoreService.removeScoreListener(this);
        mTeamService.removeTeamListener(this);
        mTimeoutService.removeTimeoutListener(this);
    }

    @Override
    public void loadRecordedGames() {
        mRecordedGames.clear();
        mRecordedGames.addAll(JsonHistoryReader.readRecordedGames(mContext, GAMES_HISTORY_FILE));
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
    public void deleteRecordedGame(long gameDate) {
        for (Iterator<RecordedGame> iterator = mRecordedGames.iterator(); iterator.hasNext();) {
            RecordedGame recordedGame = iterator.next();
            if (recordedGame.getGameDate() == gameDate) {
                iterator.remove();
            }
        }
        JsonHistoryWriter.writeRecordedGames(mContext, GAMES_HISTORY_FILE, mRecordedGames);
    }

    @Override
    public void deleteAllRecordedGames() {
        mRecordedGames.clear();
        JsonHistoryWriter.writeRecordedGames(mContext, GAMES_HISTORY_FILE, mRecordedGames);
    }

    @Override
    public boolean hasCurrentGame() {
        File currentGameFile = mContext.getFileStreamPath(GamesHistoryService.CURRENT_GAME_FILE);
        return currentGameFile != null && currentGameFile.exists();
    }

    @Override
    public void resumeCurrentGame() {
        Game game  = SerializedGameReader.readGame(mContext, CURRENT_GAME_FILE);

        if (game != null) {
            ServicesProvider.getInstance().setScoreService(game);
            ServicesProvider.getInstance().setTeamService(game);
            ServicesProvider.getInstance().setTimeoutService(game);
        }
    }

    @Override
    public void saveCurrentGame() {
        updateRecordedGame();
        if (!mScoreService.isMatchCompleted()) {
            SerializedGameWriter.writeGame(mContext, CURRENT_GAME_FILE, (Game) mScoreService);
        }
    }

    @Override
    public void deleteCurrentGame() {
        Log.d("VBR-History", String.format("Delete serialized game in %s", CURRENT_GAME_FILE));
        mContext.deleteFile(CURRENT_GAME_FILE);
        mRecordedGame = null;
    }

    @Override
    public void onMatchCompleted(TeamType winner) {
        if (mRecordedGame != null) {
            mRecordedGames.add(mRecordedGame);
        }
        JsonHistoryWriter.writeRecordedGames(mContext, GAMES_HISTORY_FILE, mRecordedGames);
        deleteCurrentGame();
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        saveCurrentGame();
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType) {}

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
    }

    @Override
    public void onTeamRotated(TeamType teamType) {
        saveCurrentGame();
    }

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {
        saveCurrentGame();
    }

    @Override
    public void onTimeout(TeamType teamType, int duration) {}

    @Override
    public void onTechnicalTimeout(int duration) {}

    @Override
    public void onGameInterval(int duration) {}

    private void createRecordedGame() {
        mRecordedGame = new RecordedGame();
        mRecordedGame.setGameType(mScoreService.getGameType());
        mRecordedGame.setGameDate(mScoreService.getGameDate());

        RecordedTeam homeTeam = mRecordedGame.getTeam(TeamType.HOME);
        homeTeam.setName(mTeamService.getTeamName(TeamType.HOME));
        homeTeam.setColor(mTeamService.getTeamColor(TeamType.HOME));

        RecordedTeam guestTeam = mRecordedGame.getTeam(TeamType.GUEST);
        guestTeam.setName(mTeamService.getTeamName(TeamType.GUEST));
        guestTeam.setColor(mTeamService.getTeamColor(TeamType.GUEST));

        if (mTeamService instanceof IndoorTeamService) {
            IndoorTeamService indoorTeamService = (IndoorTeamService) mTeamService;

            homeTeam.setLiberoColor(indoorTeamService.getLiberoColor(TeamType.HOME));
            guestTeam.setLiberoColor(indoorTeamService.getLiberoColor(TeamType.GUEST));

            for (int number : mTeamService.getPlayers(TeamType.HOME)) {
                if (indoorTeamService.isLibero(TeamType.HOME, number)) {
                    homeTeam.getLiberos().add(number);
                } else {
                    homeTeam.getPlayers().add(number);
                }
            }
            for (int number : mTeamService.getPlayers(TeamType.GUEST)) {
                if (indoorTeamService.isLibero(TeamType.GUEST, number)) {
                    guestTeam.getLiberos().add(number);
                } else {
                    guestTeam.getPlayers().add(number);
                }
            }

            homeTeam.setCaptain(indoorTeamService.getCaptain(TeamType.HOME));
            guestTeam.setCaptain(indoorTeamService.getCaptain(TeamType.GUEST));
        } else {
            homeTeam.getPlayers().addAll(mTeamService.getPlayers(TeamType.HOME));
            guestTeam.getPlayers().addAll(mTeamService.getPlayers(TeamType.GUEST));
        }

        updateRecordedGame();
    }

    private void updateRecordedGame() {
        if (mRecordedGame != null) {
            mRecordedGame.setMatchCompleted(mScoreService.isMatchCompleted());
            mRecordedGame.setSets(TeamType.HOME, mScoreService.getSets(TeamType.HOME));
            mRecordedGame.setSets(TeamType.GUEST, mScoreService.getSets(TeamType.GUEST));

            mRecordedGame.getSets().clear();

            for (int setIndex = 0 ; setIndex < mScoreService.getNumberOfSets(); setIndex++) {
                RecordedSet set = new RecordedSet();

                set.setDuration(mScoreService.getSetDuration(setIndex));
                set.getPointsLadder().addAll(mScoreService.getPointsLadder(setIndex));
                set.setServingTeam(mScoreService.getServingTeam());

                set.setPoints(TeamType.HOME, mScoreService.getPoints(TeamType.HOME, setIndex));
                set.setTimeouts(TeamType.HOME, mTimeoutService.getTimeouts(TeamType.HOME, setIndex));
                for (int number : mTeamService.getPlayersOnCourt(TeamType.HOME, setIndex)) {
                    RecordedPlayer player = new RecordedPlayer();
                    player.setNumber(number);
                    player.setPositionType(mTeamService.getPlayerPosition(TeamType.HOME, number));
                    set.getCurrentPlayers(TeamType.HOME).add(player);
                }

                set.setPoints(TeamType.GUEST, mScoreService.getPoints(TeamType.GUEST, setIndex));
                set.setTimeouts(TeamType.GUEST, mTimeoutService.getTimeouts(TeamType.GUEST, setIndex));
                for (int number : mTeamService.getPlayersOnCourt(TeamType.GUEST, setIndex)) {
                    RecordedPlayer player = new RecordedPlayer();
                    player.setNumber(number);
                    player.setPositionType(mTeamService.getPlayerPosition(TeamType.GUEST, number));
                    set.getCurrentPlayers(TeamType.GUEST).add(player);
                }

                if (mTeamService instanceof IndoorTeamService) {
                    IndoorTeamService indoorTeamService = (IndoorTeamService) mTeamService;

                    for (int number : indoorTeamService.getPlayersInStartingLineup(TeamType.HOME, setIndex)) {
                        RecordedPlayer player = new RecordedPlayer();
                        player.setNumber(number);
                        player.setPositionType(indoorTeamService.getPlayerPositionInStartingLineup(TeamType.HOME, number, setIndex));
                        set.getStartingPlayers(TeamType.HOME).add(player);
                    }

                    for (Substitution substitution : indoorTeamService.getSubstitutions(TeamType.HOME, setIndex)) {
                        Substitution sub = new Substitution(substitution.getPlayerIn(), substitution.getPlayerOut());
                        set.getSubstitutions(TeamType.HOME).add(sub);
                    }

                    for (int number : indoorTeamService.getPlayersInStartingLineup(TeamType.GUEST, setIndex)) {
                        RecordedPlayer player = new RecordedPlayer();
                        player.setNumber(number);
                        player.setPositionType(indoorTeamService.getPlayerPositionInStartingLineup(TeamType.GUEST, number, setIndex));
                        set.getStartingPlayers(TeamType.GUEST).add(player);
                    }

                    for (Substitution substitution : indoorTeamService.getSubstitutions(TeamType.GUEST, setIndex)) {
                        Substitution sub = new Substitution(substitution.getPlayerIn(), substitution.getPlayerOut());
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
