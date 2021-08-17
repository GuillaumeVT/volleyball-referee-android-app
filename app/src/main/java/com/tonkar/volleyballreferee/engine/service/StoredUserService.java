package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.ApiFriend;
import com.tonkar.volleyballreferee.engine.api.model.ApiFriendRequest;
import com.tonkar.volleyballreferee.engine.api.model.ApiNewUser;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserPasswordUpdate;

import java.util.List;

public interface StoredUserService {

    void getUser(String purchaseToken, AsyncUserRequestListener listener);

    void createUser(ApiNewUser newUser, AsyncUserRequestListener listener);

    void signInUser(String email, String password, AsyncUserRequestListener listener);

    void initiateUserPasswordRecovery(String email, AsyncUserRequestListener listener);

    void updateUserPassword(ApiUserPasswordUpdate passwordUpdate, AsyncUserRequestListener listener);

    void syncUser();

    void downloadFriendsAndRequests(AsyncFriendRequestListener listener);

    List<ApiFriend> listReferees();

    void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener);

    void acceptFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void rejectFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void removeFriend(ApiFriend friend, AsyncFriendRequestListener listener);

}
