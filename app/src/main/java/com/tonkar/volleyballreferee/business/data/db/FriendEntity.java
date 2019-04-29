package com.tonkar.volleyballreferee.business.data.db;

import androidx.room.Entity;
import com.tonkar.volleyballreferee.api.ApiFriend;

@Entity(tableName = "friends")
public class FriendEntity extends ApiFriend {

    public FriendEntity() {
        super();
    }
}
