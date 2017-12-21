package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.util.Log;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.game.Game;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.TimeoutListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamesHistory implements GamesHistoryService, ScoreListener, TeamListener, TimeoutListener {

    private final Context            mContext;
    private       GameService        mGameService;
    private       RecordedGame       mRecordedGame;
    private final List<RecordedGame> mRecordedGames;

    public GamesHistory(Context context) {
        mContext = context;
        mRecordedGames = new ArrayList<>();
    }

    @Override
    public void connectGameRecorder() {
        Log.i("VBR-History", "Connect the game recorder");
        if (hasSetupGame()) {
            deleteSetupGame();
        }
        mGameService = ServicesProvider.getInstance().getGameService();
        mGameService.addScoreListener(this);
        mGameService.addTeamListener(this);
        mGameService.addTimeoutListener(this);
        createRecordedGame();
        saveCurrentGame();
    }

    @Override
    public void disconnectGameRecorder() {
        Log.i("VBR-History", "Disconnect the game recorder");
        saveCurrentGame();
        mGameService.removeScoreListener(this);
        mGameService.removeTeamListener(this);
        mGameService.removeTimeoutListener(this);
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
    public GameService loadCurrentGame() {
        return SerializedGameReader.readGame(mContext, CURRENT_GAME_FILE);
    }

    @Override
    public void saveCurrentGame() {
        updateRecordedGame();
        if (!mGameService.isMatchCompleted()) {
            SerializedGameWriter.writeGame(mContext, CURRENT_GAME_FILE, (Game) mGameService);
        }
    }

    @Override
    public void deleteCurrentGame() {
        Log.d("VBR-History", String.format("Delete serialized game in %s", CURRENT_GAME_FILE));
        mContext.deleteFile(CURRENT_GAME_FILE);
        mRecordedGame = null;
    }

    @Override
    public boolean hasSetupGame() {
        File setupGameFile = mContext.getFileStreamPath(GamesHistoryService.SETUP_GAME_FILE);
        return setupGameFile != null && setupGameFile.exists();
    }

    @Override
    public GameService loadSetupGame() {
        return SerializedGameReader.readGame(mContext, SETUP_GAME_FILE);
    }

    @Override
    public void saveSetupGame(GameService gameService) {
        SerializedGameWriter.writeGame(mContext, SETUP_GAME_FILE, (Game) gameService);
    }

    @Override
    public void deleteSetupGame() {
        Log.d("VBR-History", String.format("Delete serialized setup game in %s", SETUP_GAME_FILE));
        mContext.deleteFile(SETUP_GAME_FILE);
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
        mRecordedGame.setGameType(mGameService.getGameType());
        mRecordedGame.setGameDate(mGameService.getGameDate());
        mRecordedGame.setGenderType(mGameService.getGenderType());

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
                set.setTimeouts(TeamType.HOME, mGameService.getTimeouts(TeamType.HOME, setIndex));
                for (int number : mGameService.getPlayersOnCourt(TeamType.HOME, setIndex)) {
                    RecordedPlayer player = new RecordedPlayer();
                    player.setNumber(number);
                    player.setPositionType(mGameService.getPlayerPosition(TeamType.HOME, number, setIndex));
                    set.getCurrentPlayers(TeamType.HOME).add(player);
                }

                set.setPoints(TeamType.GUEST, mGameService.getPoints(TeamType.GUEST, setIndex));
                set.setTimeouts(TeamType.GUEST, mGameService.getTimeouts(TeamType.GUEST, setIndex));
                for (int number : mGameService.getPlayersOnCourt(TeamType.GUEST, setIndex)) {
                    RecordedPlayer player = new RecordedPlayer();
                    player.setNumber(number);
                    player.setPositionType(mGameService.getPlayerPosition(TeamType.GUEST, number, setIndex));
                    set.getCurrentPlayers(TeamType.GUEST).add(player);
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
