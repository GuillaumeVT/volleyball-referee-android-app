package com.tonkar.volleyballreferee.engine.database.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import lombok.*;

@Entity(tableName = "friends")
@Getter
@Setter
public class FriendEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @NonNull
    @ColumnInfo(name = "pseudo")
    private String pseudo;
}
