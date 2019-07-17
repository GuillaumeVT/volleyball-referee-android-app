package com.tonkar.volleyballreferee.engine.stored;

import com.tonkar.volleyballreferee.engine.stored.api.ApiFriend;
import com.tonkar.volleyballreferee.engine.stored.api.ApiFriendRequest;
import com.tonkar.volleyballreferee.engine.stored.api.ApiNewUser;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserPasswordUpdate;

import java.util.List;

public interface StoredUserService {

    void getUser(String purchaseToken, AsyncUserRequestListener listener);

    void createUser(ApiNewUser newUser, AsyncUserRequestListener listener);

    void signInUser(String email, String password, AsyncUserRequestListener listener);

    void initiatedUserPasswordRecovery(String email, AsyncUserRequestListener listener);

    void updateUserPassword(ApiUserPasswordUpdate passwordUpdate, AsyncUserRequestListener listener);

    void syncUser();

    void downloadFriendsAndRequests(AsyncFriendRequestListener listener);

    List<ApiFriend> listReferees();

    void sendFriendRequest(String friendPseudo, AsyncFriendRequestListener listener);

    void acceptFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void rejectFriendRequest(ApiFriendRequest friendRequest, AsyncFriendRequestListener listener);

    void removeFriend(ApiFriend friend, AsyncFriendRequestListener listener);

}
