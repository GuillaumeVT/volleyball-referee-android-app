package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.business.data.GameDescription;
import com.tonkar.volleyballreferee.interfaces.GameService;

import java.util.List;
import java.util.Set;

public interface RecordedGamesService {

    void createCurrentGame(GameService gameService);

    void connectGameRecorder(GameService gameService);

    void disconnectGameRecorder(boolean exiting);

    void migrateRecordedGames();

    boolean hasRecordedGames();

    List<RecordedGameService> getRecordedGameServiceList();

    RecordedGameService getRecordedGameService(long gameDate);

    Set<String> getRecordedLeagues();

    Set<String> getRecordedDivisions();

    void deleteRecordedGame(long gameDate);

    void deleteAllRecordedGames();

    boolean hasCurrentGame();

    GameService loadCurrentGame();

    void saveCurrentGame();

    void deleteCurrentGame();

    boolean hasSetupGame();

    GameService loadSetupGame();

    void saveSetupGame(GameService gameService);

    void deleteSetupGame();

    void syncGamesOnline();

    void syncGamesOnline(DataSynchronizationListener listener);

    boolean isGameIndexed(long gameDate);

    void toggleGameIndexed(long gameDate);

    void getGameFromCode(int code, AsyncGameRequestListener listener);

    void getUserGame(long id, AsyncGameRequestListener listener);

    void getUserScheduledGames(AsyncGameRequestListener listener);
    
    void scheduleUserGameOnline(GameDescription gameDescription, DataSynchronizationListener listener);
}
