package com.tonkar.volleyballreferee.business.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SyncEntity syncEntity);

    @Query("DELETE FROM syncs WHERE item = :item AND type = :type")
    void deleteByItemAndType(String item, String type);

    @Query("DELETE FROM syncs WHERE type = :type")
    void deleteByType(String type);

    @Query("SELECT COUNT(*) FROM syncs WHERE item = :item AND type = :type")
    int countByItemAndType(String item, String type);
}
