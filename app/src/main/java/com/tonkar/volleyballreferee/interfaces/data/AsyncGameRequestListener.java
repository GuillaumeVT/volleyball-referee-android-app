package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiGameDescription;

import java.util.List;

public interface AsyncGameRequestListener {

    void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService);

    void onUserGameReceived(RecordedGameService recordedGameService);

    void onUserGameListReceived(List<ApiGameDescription> gameDescriptionList);

    void onNotFound();

    void onInternalError();

    void onError();

}
