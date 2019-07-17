package com.tonkar.volleyballreferee.engine.stored;

import com.tonkar.volleyballreferee.engine.stored.api.ApiGameSummary;

import java.util.List;

public interface AsyncGameRequestListener {

    void onGameReceived(IStoredGame storedGame);

    void onAvailableGamesReceived(List<ApiGameSummary> gameDescriptionList);

    void onError(int httpCode);

}
