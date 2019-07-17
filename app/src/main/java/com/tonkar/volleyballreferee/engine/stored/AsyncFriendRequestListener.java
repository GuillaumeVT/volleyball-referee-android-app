package com.tonkar.volleyballreferee.engine.stored;

import com.tonkar.volleyballreferee.engine.stored.api.ApiFriend;
import com.tonkar.volleyballreferee.engine.stored.api.ApiFriendRequest;
import com.tonkar.volleyballreferee.engine.stored.api.ApiFriendsAndRequests;

public interface AsyncFriendRequestListener {

    void onFriendsAndRequestsReceived(ApiFriendsAndRequests friendsAndRequests);

    void onFriendRequestSent(String friendPseudo);

    void onFriendRequestAccepted(ApiFriendRequest friendRequest);

    void onFriendRequestRejected(ApiFriendRequest friendRequest);

    void onFriendRemoved(ApiFriend friend);

    void onError(int httpCode);

}
