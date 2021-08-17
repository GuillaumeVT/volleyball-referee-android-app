package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.ApiFriend;
import com.tonkar.volleyballreferee.engine.api.model.ApiFriendRequest;
import com.tonkar.volleyballreferee.engine.api.model.ApiFriendsAndRequests;

public interface AsyncFriendRequestListener {

    void onFriendsAndRequestsReceived(ApiFriendsAndRequests friendsAndRequests);

    void onFriendRequestSent(String friendPseudo);

    void onFriendRequestAccepted(ApiFriendRequest friendRequest);

    void onFriendRequestRejected(ApiFriendRequest friendRequest);

    void onFriendRemoved(ApiFriend friend);

    void onError(int httpCode);

}
