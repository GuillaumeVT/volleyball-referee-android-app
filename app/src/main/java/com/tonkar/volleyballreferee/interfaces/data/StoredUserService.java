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

    void sendFriendRequest(String friendPseudo);

    void acceptFriendRequest(ApiFriendRequest friendRequest);

    void rejectFriendRequest(ApiFriendRequest friendRequest);

    void removeFriend(String friendId);

}
