package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiUser;

public interface AsyncUserRequestListener {

    void onUserCreated(ApiUser user);

    void onUserReceived(ApiUser user);

    void onError(int httpCode);

}
