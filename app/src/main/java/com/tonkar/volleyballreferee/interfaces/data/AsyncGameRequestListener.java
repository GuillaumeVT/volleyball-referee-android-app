package com.tonkar.volleyballreferee.interfaces.data;

public interface AsyncGameRequestListener {

    void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService);

    void onTechnicalError();

    void onInvalidCode();

}
