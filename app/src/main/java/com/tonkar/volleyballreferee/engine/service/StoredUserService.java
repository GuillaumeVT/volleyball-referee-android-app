package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;

import java.util.List;

public interface StoredUserService {

    void signInUser(String email, String password, AsyncUserRequestListener listener);

    void updateUserPassword(ApiUserPasswordUpdate passwordUpdate, AsyncUserRequestListener listener);

    void syncUser();

    void downloadFriendsAndRequests(AsyncFriendRequestListener listener);

    List<ApiFriend> listReferees();

    void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener);

    void acceptFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void rejectFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void removeFriend(ApiFriend friend, AsyncFriendRequestListener listener);

}
