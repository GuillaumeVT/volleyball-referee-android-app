package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiGameDescription;
import com.tonkar.volleyballreferee.interfaces.GameService;

import java.util.List;

public interface StoredGamesService {

    void createCurrentGame(GameService gameService);

    void connectGameRecorder(GameService gameService);

    void disconnectGameRecorder(boolean exiting);

    boolean hasGames();

    List<ApiGameDescription> listGames();

    StoredGameService getCurrentGame();

    StoredGameService getGame(String id);

    void deleteGame(String id);

    void deleteAllGames();

    boolean hasCurrentGame();

    GameService loadCurrentGame();

    void saveCurrentGame();

    void deleteCurrentGame();

    boolean hasSetupGame();

    GameService loadSetupGame();

    void saveSetupGame(GameService gameService);

    void deleteSetupGame();

    void syncGames();

    void syncGames(DataSynchronizationListener listener);

    boolean isGameIndexed(String id);

    void toggleGameIndexed(String id);

    void downloadGame(String id, AsyncGameRequestListener listener);

    void downloadAvailableGames(AsyncGameRequestListener listener);
    
    void scheduleGame(ApiGameDescription gameDescription, boolean create, DataSynchronizationListener listener);

    void cancelGame(String id, DataSynchronizationListener listener);
}
