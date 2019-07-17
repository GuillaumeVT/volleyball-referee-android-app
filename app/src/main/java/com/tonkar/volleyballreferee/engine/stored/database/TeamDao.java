package com.tonkar.volleyballreferee.engine.stored.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeamSummary;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import java.util.List;

@Dao
public interface TeamDao {

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind, gender FROM teams ORDER BY name ASC")
    List<ApiTeamSummary> listTeams();

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind, gender FROM teams WHERE kind = :kind ORDER BY name ASC")
    List<ApiTeamSummary> listTeamsByKind(GameType kind);

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind, gender FROM teams WHERE gender = :gender AND kind = :kind ORDER BY name ASC")
    List<ApiTeamSummary> listTeamsByGenderAndKind(GenderType gender, GameType kind);

    @Query("SELECT content FROM teams WHERE id = :id")
    String findContentById(String id);

    @Query("SELECT content FROM teams WHERE name = :name AND gender = :gender AND kind = :kind")
    String findContentByNameAndGenderAndKind(String name, GenderType gender, GameType kind);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TeamEntity teamEntity);

    @Query("DELETE FROM teams")
    void deleteAll();

    @Query("DELETE FROM teams WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM teams")
    int count();

    @Query("SELECT COUNT(*) FROM teams WHERE name = :name AND gender = :gender AND kind = :kind")
    int countByNameAndGenderAndKind(String name, GenderType gender, GameType kind);

    @Query("SELECT COUNT(*) FROM rules WHERE id = :id")
    int countById(String id);
}