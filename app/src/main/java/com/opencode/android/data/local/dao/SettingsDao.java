package com.opencode.android.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.opencode.android.data.local.entity.SettingsEntity;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Settings operations.
 */
@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SettingsEntity setting);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SettingsEntity> settings);

    @Update
    void update(SettingsEntity setting);

    @Delete
    void delete(SettingsEntity setting);

    @Query("DELETE FROM settings WHERE `key` = :key")
    void deleteByKey(String key);

    @Query("DELETE FROM settings")
    void deleteAll();

    @Query("SELECT * FROM settings WHERE `key` = :key")
    LiveData<SettingsEntity> getByKey(String key);

    @Query("SELECT * FROM settings WHERE `key` = :key")
    SettingsEntity getByKeySync(String key);

    @Query("SELECT * FROM settings WHERE `key` = :key")
    Optional<SettingsEntity> getByKeyOptional(String key);

    @Query("SELECT value FROM settings WHERE `key` = :key")
    String getValueByKeySync(String key);

    @Query("SELECT * FROM settings ORDER BY `key` ASC")
    LiveData<List<SettingsEntity>> getAll();

    @Query("SELECT * FROM settings WHERE category = :category ORDER BY `key` ASC")
    LiveData<List<SettingsEntity>> getByCategory(String category);

    @Query("SELECT * FROM settings WHERE `key` LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%'")
    LiveData<List<SettingsEntity>> search(String query);

    @Query("SELECT EXISTS(SELECT 1 FROM settings WHERE `key` = :key)")
    boolean existsSync(String key);

    @Query("SELECT COUNT(*) FROM settings")
    LiveData<Integer> getCount();

    @Query("UPDATE settings SET value = :value, updated_at = :timestamp WHERE `key` = :key")
    void updateValue(String key, String value, long timestamp);

    @Query("UPDATE settings SET value = :value WHERE `key` = :key")
    void updateValue(String key, String value);

    // Convenience methods for common settings
    @Query("SELECT value FROM settings WHERE `key` = 'api_key'")
    String getApiKeySync();

    @Query("SELECT value FROM settings WHERE `key` = 'api_base_url'")
    String getApiBaseUrlSync();

    @Query("SELECT value FROM settings WHERE `key` = 'default_model'")
    String getDefaultModelSync();

    @Query("SELECT value FROM settings WHERE `key` = 'theme'")
    String getThemeSync();
}
