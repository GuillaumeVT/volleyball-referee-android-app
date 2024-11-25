package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.GameSummaryDto;
import com.tonkar.volleyballreferee.engine.game.IGame;

import java.util.*;

public interface StoredGamesService {

    void createCurrentGame(IGame game);

    void connectGameRecorder(IGame game);

    void disconnectGameRecorder(boolean exiting);

    List<GameSummaryDto> listGames();

    IStoredGame getCurrentGame();

    IStoredGame getGame(String id);

    void deleteGame(String id);

    void deleteGames(Set<String> ids, DataSynchronizationListener listener);

    boolean hasCurrentGame();

    IGame loadCurrentGame();

    void saveCurrentGame(boolean syncInsertion);

    void saveCurrentGame();

    void deleteCurrentGame();

    boolean hasSetupGame();

    IGame loadSetupGame();

    void saveSetupGame(IGame game);

    void deleteSetupGame();

    void syncGames();

    void syncGames(DataSynchronizationListener listener);

    void downloadGame(String id, AsyncGameRequestListener listener);

    void downloadAvailableGames(AsyncGameRequestListener listener);

    void scheduleGame(GameSummaryDto gameDescription, boolean create, DataSynchronizationListener listener);

    void cancelGame(String id, DataSynchronizationListener listener);
}
