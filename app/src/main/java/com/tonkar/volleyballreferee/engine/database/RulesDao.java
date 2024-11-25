package com.tonkar.volleyballreferee.engine.database;

import androidx.room.*;

import com.tonkar.volleyballreferee.engine.api.model.RulesSummaryDto;
import com.tonkar.volleyballreferee.engine.database.model.RulesEntity;
import com.tonkar.volleyballreferee.engine.game.GameType;

import java.util.*;

@Dao
public interface RulesDao {

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind FROM rules ORDER BY name ASC")
    List<RulesSummaryDto> listRules();

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind FROM rules WHERE kind = :kind ORDER BY name ASC")
    List<RulesSummaryDto> listRulesByKind(GameType kind);

    @Query("SELECT content FROM rules WHERE id = :id")
    String findContentById(String id);

    @Query("SELECT content FROM rules WHERE name = :name AND kind = :kind")
    String findContentByNameAndKind(String name, GameType kind);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RulesEntity rulesEntity);

    @Query("DELETE FROM rules")
    void deleteAll();

    @Query("DELETE FROM rules WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM rules WHERE id IN (:ids)")
    void deleteByIdIn(Set<String> ids);

    @Query("SELECT COUNT(*) FROM rules")
    int count();

    @Query("SELECT COUNT(*) FROM rules WHERE name = :name AND kind = :kind")
    int countByNameAndKind(String name, GameType kind);

    @Query("SELECT COUNT(*) FROM rules WHERE id = :id")
    int countById(String id);
}