package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;

public interface AsyncFriendRequestListener {

    void onFriendsAndRequestsReceived(ApiFriendsAndRequests friendsAndRequests);

    void onFriendRequestSent(String friendPseudo);

    void onFriendRequestAccepted(ApiFriendRequest friendRequest);

    void onFriendRequestRejected(ApiFriendRequest friendRequest);

    void onFriendRemoved(ApiFriend friend);

    void onError(int httpCode);

}
