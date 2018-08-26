package com.tonkar.volleyballreferee.business.data.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface TeamDao {

    @Query("SELECT content FROM teams")
    List<String> getAllContents();

    @Query("SELECT content FROM teams WHERE kind = :kind")
    List<String> findContentByKind(String kind);

    @Query("SELECT name FROM teams WHERE gender = :gender AND kind = :kind")
    List<String> findNamesByGenderAndKind(String gender, String kind);

    @Query("SELECT content FROM teams WHERE name = :name AND gender = :gender AND kind = :kind")
    String findContentByNameAndGenderAndKind(String name, String gender, String kind);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TeamEntity> teamEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TeamEntity teamEntity);

    @Query("DELETE FROM teams")
    void deleteAll();

    @Query("DELETE FROM teams WHERE name = :name AND gender = :gender AND kind = :kind")
    void deleteByNameAndGenderAndKind(String name, String gender, String kind);

    @Query("SELECT COUNT(*) FROM teams")
    int count();

    @Query("SELECT COUNT(*) FROM teams WHERE name = :name AND gender = :gender AND kind = :kind")
    int countByNameAndGenderAndKind(String name, String gender, String kind);
}