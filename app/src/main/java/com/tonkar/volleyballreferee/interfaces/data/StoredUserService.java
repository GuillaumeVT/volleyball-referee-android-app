package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiFriend;
import com.tonkar.volleyballreferee.api.ApiFriendRequest;

import java.util.List;

public interface StoredUserService {

    void createUser(String userId, String pseudo, AsyncUserRequestListener listener);

    void downloadUser(AsyncUserRequestListener listener);

    void syncUser();

    void downloadFriendsAndRequests(AsyncFriendRequestListener listener);

    boolean hasFriends();

    List<ApiFriend> listReferees();

    void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener);

    void acceptFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void rejectFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void removeFriend(ApiFriend friend, AsyncFriendRequestListener listener);

}
