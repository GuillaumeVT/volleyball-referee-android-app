package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiFriend;
import com.tonkar.volleyballreferee.api.ApiFriendRequest;
import com.tonkar.volleyballreferee.api.ApiFriendsAndRequests;

public interface AsyncFriendRequestListener {

    void onFriendsAndRequestsReceived(ApiFriendsAndRequests friendsAndRequests);

    void onFriendRequestSent(String friendPseudo);

    void onFriendRequestAccepted(ApiFriendRequest friendRequest);

    void onFriendRequestRejected(ApiFriendRequest friendRequest);

    void onFriendRemoved(ApiFriend friend);

    void onError(int httpCode);

}
