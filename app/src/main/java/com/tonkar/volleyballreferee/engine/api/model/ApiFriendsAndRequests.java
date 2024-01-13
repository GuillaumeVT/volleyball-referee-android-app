package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.*;

import lombok.*;

@Getter
@Setter
public class ApiFriendsAndRequests {
    @SerializedName("friends")
    private List<ApiFriend>        friends;
    @SerializedName("receivedFriendRequests")
    private List<ApiFriendRequest> receivedFriendRequests;
    @SerializedName("sentFriendRequests")
    private List<ApiFriendRequest> sentFriendRequests;

    public ApiFriendsAndRequests() {
        this.friends = new ArrayList<>();
        this.receivedFriendRequests = new ArrayList<>();
        this.sentFriendRequests = new ArrayList<>();
    }
}
