package com.tonkar.volleyballreferee.interfaces;

public interface WebGamesService {

    String BASE_URL        = "http://www.volleyball-referee.com";
    String SEARCH_URL      = BASE_URL + "/search";
    String LIVE_URL        = BASE_URL + "/search/live";
    String VIEW_INDOOR_URL = BASE_URL + "/indoor/%d";
    String VIEW_BEACH_URL  = BASE_URL + "/beach/%d";
    String GAME_API_URL    = BASE_URL + "/api/manage/game/%d";
    String SET_API_URL     = GAME_API_URL + "/set/%d";

    void assessAreRecordedOnline();

    boolean isOnlineRecordingEnabled();

    void toggleOnlineRecording();

    boolean isGameRecordedOnline(long gameDate);

    void uploadRecordedGameOnline(long gameDate);
}
