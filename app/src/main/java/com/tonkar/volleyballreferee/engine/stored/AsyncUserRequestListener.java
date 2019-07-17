package com.tonkar.volleyballreferee.engine.stored;

import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserToken;

public interface AsyncUserRequestListener {

    void onUserReceived(ApiUserSummary user);

    void onUserTokenReceived(ApiUserToken userToken);

    void onUserPasswordRecoveryInitiated();

    void onError(int httpCode);

}
