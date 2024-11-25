package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.UserTokenDto;

public interface AsyncUserRequestListener {

    void onUserTokenReceived(UserTokenDto userToken);

    void onError(int httpCode);

}
