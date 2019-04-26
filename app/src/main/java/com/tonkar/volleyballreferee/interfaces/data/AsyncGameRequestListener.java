package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiGameDescription;

import java.util.List;

public interface AsyncGameRequestListener {

    void onGameReceived(StoredGameService storedGameService);

    void onAvailableGamesReceived(List<ApiGameDescription> gameDescriptionList);

    void onNotFound();

    void onInternalError();

    void onError();

}
