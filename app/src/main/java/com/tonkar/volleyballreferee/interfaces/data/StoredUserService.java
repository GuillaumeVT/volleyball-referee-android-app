package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiFriend;
import com.tonkar.volleyballreferee.api.ApiFriendRequest;

import java.util.List;

public interface StoredUserService {

    void createUser(String userId, String pseudo, AsyncUserRequestListener listener);

    void downloadUser(AsyncUserRequestListener listener);

    void syncUser();

    void downloadFriendRequests(AsyncUserRequestListener listener);

    boolean hasFriends();

    List<ApiFriend> listFriends();

    List<ApiFriend> listReferees();

    void sendFriendRequest(String friendPseudo, DataSynchronizationListener listener);

    void acceptFriendRequest(ApiFriendRequest friendRequest, DataSynchronizationListener listener);

    void rejectFriendRequest(ApiFriendRequest friendRequest, DataSynchronizationListener listener);

    void removeFriend(String friendId, DataSynchronizationListener listener);

}
