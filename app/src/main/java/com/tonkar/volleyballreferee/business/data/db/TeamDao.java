package com.tonkar.volleyballreferee.business.data.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.tonkar.volleyballreferee.api.ApiTeamDescription;

@Dao
public interface TeamDao {

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind, gender FROM teams ORDER BY name ASC")
    List<ApiTeamDescription> listTeams();

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind, gender FROM teams WHERE kind = :kind ORDER BY name ASC")
    List<ApiTeamDescription> listTeamsByKind(String kind);

    @Query("SELECT name FROM teams WHERE gender = :gender AND kind = :kind ORDER BY name ASC")
    List<String> listNamesByGenderAndKind(String gender, String kind);

    @Query("SELECT content FROM teams WHERE id = :id")
    String findContentById(String id);

    @Query("SELECT content FROM teams WHERE name = :name AND gender = :gender AND kind = :kind")
    String findContentByNameAndGenderAndKind(String name, String gender, String kind);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TeamEntity teamEntity);

    @Query("DELETE FROM teams")
    void deleteAll();

    @Query("DELETE FROM teams WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM teams")
    int count();

    @Query("SELECT COUNT(*) FROM teams WHERE name = :name AND gender = :gender AND kind = :kind")
    int countByNameAndGenderAndKind(String name, String gender, String kind);
}