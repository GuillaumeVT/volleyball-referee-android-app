package com.tonkar.volleyballreferee.business.data.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.tonkar.volleyballreferee.api.ApiRulesDescription;

@Dao
public interface RulesDao {

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind FROM rules ORDER BY name ASC")
    List<ApiRulesDescription> listRules();

    @Query("SELECT id, createdBy, createdAt, updatedAt, synced, name, kind FROM rules WHERE kind = :kind ORDER BY name ASC")
    List<ApiRulesDescription> listRulesByKind(String kind);

    @Query("SELECT name FROM rules WHERE kind = :kind ORDER BY name ASC")
    List<String> listNamesByKind(String kind);

    @Query("SELECT content FROM rules WHERE id = :id")
    String findContentById(String id);

    @Query("SELECT content FROM rules WHERE name = :name AND kind = :kind")
    String findContentByNameAndKind(String name, String kind);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RulesEntity> rulesEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RulesEntity rulesEntity);

    @Query("DELETE FROM rules")
    void deleteAll();

    @Query("DELETE FROM rules WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM rules")
    int count();

    @Query("SELECT COUNT(*) FROM rules WHERE name = :name AND kind = :kind")
    int countByNameAndKind(String name, String kind);
}