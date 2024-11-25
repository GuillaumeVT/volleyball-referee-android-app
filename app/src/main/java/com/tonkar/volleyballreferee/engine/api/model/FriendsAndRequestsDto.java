package com.tonkar.volleyballreferee.engine.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.*;

import lombok.*;

@Getter
@Setter
public class FriendsAndRequestsDto {
    @SerializedName("friends")
    private List<FriendDto>        friends;
    @SerializedName("receivedFriendRequests")
    private List<FriendRequestDto> receivedFriendRequests;
    @SerializedName("sentFriendRequests")
    private List<FriendRequestDto> sentFriendRequests;

    public FriendsAndRequestsDto() {
        this.friends = new ArrayList<>();
        this.receivedFriendRequests = new ArrayList<>();
        this.sentFriendRequests = new ArrayList<>();
    }
}
