package com.tonkar.volleyballreferee.interfaces.data;

public interface WebGamesService {

    void assessAreRecordedOnline();

    boolean isOnlineRecordingEnabled();

    void toggleOnlineRecording();

    boolean isGameRecordedOnline(long gameDate);

    void uploadRecordedGameOnline(long gameDate);
}
