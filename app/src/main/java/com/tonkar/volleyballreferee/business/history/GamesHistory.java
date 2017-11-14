package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.util.Log;

import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.business.game.Game;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.GameListener;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeoutClient;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.TimeoutService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamesHistory implements GamesHistoryService, GameClient, TeamClient, TimeoutClient, GameListener, TeamListener, TimeoutListener {

    private final Context            mContext;
    private       boolean            mAutoSave;
    private       GameService        mGameService;
    private       TeamService        mTeamService;
    private       TimeoutService     mTimeoutService;
    private final List<RecordedGame> recordedGames;

    public GamesHistory(Context context) {
        mContext = context;
        recordedGames = new ArrayList<>();
        mAutoSave = true;
    }

    @Override
    public void setGameService(GameService gameService) {
        mGameService = gameService;
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
        setGameService(ServicesProvider.getInstance().getGameService());
        setTeamService(ServicesProvider.getInstance().getTeamService());
        setTimeoutService(ServicesProvider.getInstance().getTimeoutService());
        mGameService.addGameListener(this);
        mTeamService.addTeamListener(this);
        mTimeoutService.addTimeoutListener(this);
        enableAutoSaveCurrentGame();
        saveCurrentGame();
    }

    @Override
    public void disconnectGameRecorder() {
        Log.i("VBR-History", "Disconnect the game recorder");
        saveCurrentGame();
        mGameService.removeGameListener(this);
        mTeamService.removeTeamListener(this);
        mTimeoutService.removeTimeoutListener(this);
    }

    @Override
    public void loadRecordedGames() {
        recordedGames.clear();
        recordedGames.addAll(JsonHistoryReader.readRecordedGames(mContext, GAMES_HISTORY_FILE));
    }

    @Override
    public List<RecordedGameService> getRecordedGameServiceList() {
        List<RecordedGameService> list = new ArrayList<>();
        list.addAll(recordedGames);
        return list;
    }

    @Override
    public RecordedGameService getRecordedGameService(long gameDate) {
        RecordedGame matching = null;

        for (RecordedGame recordedGame : recordedGames) {
            if (recordedGame.getGameDate() == gameDate) {
                matching = recordedGame;
            }
        }

        return matching;
    }

    @Override
    public void deleteRecordedGame(long gameDate) {
        for (Iterator<RecordedGame> iterator = recordedGames.iterator(); iterator.hasNext();) {
            RecordedGame recordedGame = iterator.next();
            if (recordedGame.getGameDate() == gameDate) {
                iterator.remove();
            }
        }
        JsonHistoryWriter.writeRecordedGames(mContext, GAMES_HISTORY_FILE, recordedGames);
    }

    @Override
    public void deleteAllRecordedGames() {
        recordedGames.clear();
        JsonHistoryWriter.writeRecordedGames(mContext, GAMES_HISTORY_FILE, recordedGames);
    }

    @Override
    public void resumeCurrentGame() {
        Game game  = SerializedGameReader.readGame(mContext, CURRENT_GAME_FILE);

        if (game != null) {
            ServicesProvider.getInstance().setGameService(game);
            ServicesProvider.getInstance().setTeamService(game);
            ServicesProvider.getInstance().setTimeoutService(game);
        }
    }

    @Override
    public void saveCurrentGame() {
        if (mAutoSave && !mGameService.isGameCompleted()) {
            SerializedGameWriter.writeGame(mContext, CURRENT_GAME_FILE, (Game) mGameService);
        }
    }

    @Override
    public void deleteCurrentGame() {
        Log.d("VBR-History", String.format("Delete serialized game in %s", CURRENT_GAME_FILE));
        mContext.deleteFile(CURRENT_GAME_FILE);
    }

    @Override
    public void enableAutoSaveCurrentGame() {
        Log.d("VBR-History", "Enable auto-save");
        mAutoSave = true;
    }

    @Override
    public void disableAutoSaveCurrentGame() {
        Log.d("VBR-History", "Disable auto-save");
        mAutoSave = false;
    }

    @Override
    public void onGameCompleted(TeamType winner) {
        RecordedGame recordedGame = createRecordedGame();
        recordedGames.add(recordedGame);
        JsonHistoryWriter.writeRecordedGames(mContext, GAMES_HISTORY_FILE, recordedGames);
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
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType) {
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
    public void onTimeout(int duration) {}

    private RecordedGame createRecordedGame() {
        RecordedTeam homeTeam = new RecordedTeam(mTeamService.getTeamName(TeamType.HOME), mTeamService.getTeamColor(TeamType.HOME));
        RecordedTeam guestTeam = new RecordedTeam(mTeamService.getTeamName(TeamType.GUEST), mTeamService.getTeamColor(TeamType.GUEST));

        List<RecordedSet> sets = new ArrayList<>();

        for (int setIndex = 0; setIndex < mGameService.getNumberOfSets(); setIndex++) {
            List<TeamType> pointsLadder = new ArrayList<>();
            pointsLadder.addAll(mGameService.getPointsLadder(setIndex));

            sets.add(new RecordedSet(mGameService.getSetDuration(setIndex), mGameService.getPoints(TeamType.HOME, setIndex), mGameService.getPoints(TeamType.GUEST, setIndex), pointsLadder));
        }

        return new RecordedGame(mGameService.getGameType(), mGameService.getGameDate(), homeTeam, guestTeam, sets);
    }

}
