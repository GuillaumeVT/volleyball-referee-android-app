package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;

public interface AsyncFriendRequestListener {

    void onFriendsAndRequestsReceived(FriendsAndRequestsDto friendsAndRequests);

    void onFriendRequestSent(String friendPseudo);

    void onFriendRequestAccepted(FriendRequestDto friendRequest);

    void onFriendRequestRejected(FriendRequestDto friendRequest);

    void onFriendRemoved(FriendDto friend);

    void onError(int httpCode);

}
