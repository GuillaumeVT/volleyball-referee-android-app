package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserToken;

public interface AsyncUserRequestListener {

    void onUserReceived(ApiUserSummary user);

    void onUserTokenReceived(ApiUserToken userToken);

    void onUserPasswordRecoveryInitiated();

    void onError(int httpCode);

}
