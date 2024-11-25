package com.tonkar.volleyballreferee.ui.user;

import com.tonkar.volleyballreferee.engine.api.model.*;

import lombok.*;

@Getter
@Setter
public class ColleagueItem {

    public enum ItemType {
        FRIEND,
        RECEIVED,
        SENT
    }

    private final ItemType         itemType;
    private final FriendDto        friend;
    private final FriendRequestDto friendRequest;

    ColleagueItem(ItemType itemType, FriendDto friend) {
        this.itemType = itemType;
        this.friend = friend;
        this.friendRequest = null;
    }

    ColleagueItem(ItemType itemType, FriendRequestDto friendRequest) {
        this.itemType = itemType;
        this.friend = null;
        this.friendRequest = friendRequest;
    }

}
