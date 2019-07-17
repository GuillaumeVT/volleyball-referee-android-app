package com.tonkar.volleyballreferee.engine.stored.database;

import androidx.room.Entity;
import com.tonkar.volleyballreferee.engine.stored.api.ApiFriend;

@Entity(tableName = "friends")
public class FriendEntity extends ApiFriend {

    public FriendEntity() {
        super();
    }
}
