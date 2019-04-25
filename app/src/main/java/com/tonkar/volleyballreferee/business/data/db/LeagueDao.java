package com.tonkar.volleyballreferee.business.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LeagueDao {

    @Query("SELECT content FROM leagues ORDER BY name ASC")
    List<String> listContents();

    @Query("SELECT name FROM leagues WHERE kind = :kind ORDER BY name ASC")
    List<String> listNamesByKind(String kind);

    @Query("SELECT content FROM leagues WHERE id = :id")
    String findContentById(String id);

    @Query("SELECT content FROM leagues WHERE name = :name AND kind = :kind")
    String findContentByNameAndKind(String name, String kind);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LeagueEntity> leagueEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LeagueEntity leagueEntity);

    @Query("DELETE FROM leagues")
    void deleteAll();

    @Query("DELETE FROM leagues WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM leagues")
    int count();

    @Query("SELECT COUNT(*) FROM leagues WHERE name = :name AND kind = :kind")
    int countByNameAndKind(String name, String kind);

}