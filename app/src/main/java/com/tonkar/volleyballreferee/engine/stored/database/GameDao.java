package com.tonkar.volleyballreferee.engine.stored.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tonkar.volleyballreferee.engine.stored.api.ApiGameSummary;

import java.util.List;

@Dao
public interface GameDao {

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, scheduledAt, kind, gender, usage, public, leagueName, divisionName, homeTeamName, guestTeamName, homeSets, guestSets, score FROM games ORDER BY scheduledAt DESC")
    List<ApiGameSummary> listGames();

    @Query("SELECT content FROM games WHERE id = :id")
    String findContentById(String id);

    @Query("SELECT public FROM games WHERE id = :id")
    boolean isGameIndexed(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GameEntity gameEntity);

    @Query("DELETE FROM games")
    void deleteAll();

    @Query("DELETE FROM games WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM games")
    int count();
}