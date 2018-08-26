package com.tonkar.volleyballreferee.business.data.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface GameDao {

    @Query("SELECT content FROM games")
    List<String> getAllContents();

    @Query("SELECT content FROM games WHERE date = :date")
    String findContentByDate(Long date);

    @Query("SELECT DISTINCT league FROM games")
    List<String> getLeagues();

    @Query("SELECT DISTINCT division FROM games")
    List<String> getDivisions();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GameEntity> gameEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GameEntity gameEntity);

    @Query("DELETE FROM games")
    void deleteAll();

    @Query("DELETE FROM games WHERE date = :date")
    void deleteByDate(Long date);

    @Query("SELECT COUNT(*) FROM games")
    int count();
}