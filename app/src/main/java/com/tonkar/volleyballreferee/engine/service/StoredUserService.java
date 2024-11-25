package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;

import java.util.List;

public interface StoredUserService {

    void signInUser(String email, String password, AsyncUserRequestListener listener);

    void updateUserPassword(UserPasswordUpdateDto passwordUpdate, AsyncUserRequestListener listener);

    void syncUser();

    void downloadFriendsAndRequests(AsyncFriendRequestListener listener);

    List<FriendDto> listReferees();

    void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener);

    void acceptFriendRequest(FriendRequestDto friendRequest, AsyncFriendRequestListener listener);

    void rejectFriendRequest(FriendRequestDto friendRequest, AsyncFriendRequestListener listener);

    void removeFriend(FriendDto friend, AsyncFriendRequestListener listener);

}
