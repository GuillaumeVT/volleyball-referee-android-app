package com.tonkar.volleyballreferee.engine.database.model;

import androidx.room.Entity;

import com.tonkar.volleyballreferee.engine.api.model.ApiFriend;

@Entity(tableName = "friends")
public class FriendEntity extends ApiFriend {

    public FriendEntity() {
        super();
    }
}
