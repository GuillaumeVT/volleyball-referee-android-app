package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.ApiGameSummary;

import java.util.List;

public interface AsyncGameRequestListener {

    void onGameReceived(IStoredGame storedGame);

    void onAvailableGamesReceived(List<ApiGameSummary> gameDescriptionList);

    void onError(int httpCode);

}
