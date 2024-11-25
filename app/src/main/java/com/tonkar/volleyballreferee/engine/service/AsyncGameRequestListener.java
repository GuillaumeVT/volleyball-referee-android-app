package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.GameSummaryDto;

import java.util.List;

public interface AsyncGameRequestListener {

    void onGameReceived(IStoredGame storedGame);

    void onAvailableGamesReceived(List<GameSummaryDto> gameDescriptionList);

    void onError(int httpCode);

}
