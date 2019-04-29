package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiFriendRequest;
import com.tonkar.volleyballreferee.api.ApiGameDescription;
import com.tonkar.volleyballreferee.api.ApiUser;

import java.util.List;

public interface AsyncUserRequestListener {

    void onUserCreated();

    void onUserReceived(ApiUser user);

    void onFriendRequestsReceived(List<ApiFriendRequest> friendRequests);

    void onNotFound();

    void onInternalError();

    void onError();

}
