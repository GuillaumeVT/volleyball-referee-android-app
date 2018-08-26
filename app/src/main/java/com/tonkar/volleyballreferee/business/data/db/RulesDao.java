package com.tonkar.volleyballreferee.business.data.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface RulesDao {

    @Query("SELECT content FROM rules")
    List<String> getAllContents();

    @Query("SELECT content FROM rules WHERE name = :name")
    String findContentByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RulesEntity> rulesEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RulesEntity rulesEntity);

    @Query("DELETE FROM rules")
    void deleteAll();

    @Query("DELETE FROM rules WHERE name = :name")
    void deleteByName(String name);

    @Query("SELECT COUNT(*) FROM rules")
    int count();

    @Query("SELECT COUNT(*) FROM rules WHERE name = :name")
    int countByName(String name);
}