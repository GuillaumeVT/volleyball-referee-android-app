package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.ApiUserToken;

public interface AsyncUserRequestListener {

    void onUserTokenReceived(ApiUserToken userToken);

    void onError(int httpCode);

}
