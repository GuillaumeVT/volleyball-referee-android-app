package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface GamesHistoryService {

    String GAMES_HISTORY_FILE = "device_games_history.json";

    String CURRENT_GAME_FILE  = "current_game.bin";

    String SETUP_GAME_FILE    = "setup_game.bin";

    void connectGameRecorder();

    void disconnectGameRecorder();

    void loadRecordedGames();

    List<RecordedGameService> getRecordedGameServiceList();

    RecordedGameService getRecordedGameService(long gameDate);

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

}
