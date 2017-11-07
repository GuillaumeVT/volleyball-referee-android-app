package com.tonkar.volleyballreferee.interfaces;

import java.util.List;

public interface GamesHistoryService {

    String GAMES_HISTORY_FILE = "games_history.json";

    String CURRENT_GAME_FILE  = "current_game.bin";

    void connectGameRecorder();

    void disconnectGameRecorder();

    void loadRecordedGames();

    List<RecordedGameService> getRecordedGameServiceList();

    RecordedGameService getRecordedGameService(long gameDate);

    void deleteRecordedGame(long gameDate);

    void deleteAllRecordedGames();

    void resumeCurrentGame();

    void saveCurrentGame();

    void deleteCurrentGame();
}
