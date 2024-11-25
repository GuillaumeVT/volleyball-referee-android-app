package com.tonkar.volleyballreferee.engine.database;

import androidx.room.*;

import com.tonkar.volleyballreferee.engine.api.model.LeagueSummaryDto;
import com.tonkar.volleyballreferee.engine.database.model.LeagueEntity;
import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.List;

@Dao
public interface LeagueDao {

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind FROM leagues ORDER BY name ASC")
    List<LeagueSummaryDto> listLeagues();

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind FROM leagues WHERE kind = :kind ORDER BY name ASC")
    List<LeagueSummaryDto> listLeaguesByKind(GameType kind);

    @Query("SELECT content FROM leagues WHERE id = :id")
    String findContentById(String id);

    @Query("SELECT content FROM leagues WHERE name = :name AND kind = :kind")
    String findContentByNameAndKind(String name, GameType kind);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LeagueEntity leagueEntity);

    @Query("DELETE FROM leagues")
    void deleteAll();

    @Query("DELETE FROM leagues WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM leagues")
    int count();

    @Query("SELECT COUNT(*) FROM leagues WHERE name = :name AND kind = :kind")
    int countByNameAndKind(String name, GameType kind);

    @Query("SELECT COUNT(*) FROM rules WHERE id = :id")
    int countById(String id);

}