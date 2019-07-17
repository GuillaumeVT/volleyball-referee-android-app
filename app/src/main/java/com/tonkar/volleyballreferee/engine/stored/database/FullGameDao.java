package com.tonkar.volleyballreferee.engine.stored.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface FullGameDao {

    @Query("SELECT content FROM full_games WHERE type = :type")
    String findContentByType(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FullGameEntity fullGameEntity);

    @Query("DELETE FROM full_games WHERE type = :type")
    void deleteByType(String type);

    @Query("SELECT COUNT(*) FROM full_games WHERE type = :type")
    int countByType(String type);
}