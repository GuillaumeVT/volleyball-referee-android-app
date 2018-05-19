package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.business.data.GameDescription;

import java.util.List;

public interface AsyncGameRequestListener {

    void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService);

    void onUserGameReceived(RecordedGameService recordedGameService);

    void onUserGameListReceived(List<GameDescription> gameDescriptionList);

    void onNotFound();

    void onInternalError();

    void onError();

}
