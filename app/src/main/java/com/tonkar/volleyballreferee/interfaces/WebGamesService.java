package com.tonkar.volleyballreferee.interfaces;

public interface WebGamesService {

    void assessAreRecordedOnline();

    boolean isOnlineRecordingEnabled();

    void toggleOnlineRecording();

    boolean isGameRecordedOnline(long gameDate);

    void uploadRecordedGameOnline(long gameDate);
}
