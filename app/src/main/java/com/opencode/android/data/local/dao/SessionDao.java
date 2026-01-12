package com.opencode.android.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.opencode.android.data.local.entity.SessionEntity;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Session operations.
 */
@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SessionEntity session);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SessionEntity> sessions);

    @Update
    void update(SessionEntity session);

    @Delete
    void delete(SessionEntity session);

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    void deleteById(String sessionId);

    @Query("DELETE FROM sessions")
    void deleteAll();

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    LiveData<SessionEntity> getById(String sessionId);

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    SessionEntity getByIdSync(String sessionId);

    @Query("SELECT * FROM sessions ORDER BY updated_at DESC")
    LiveData<List<SessionEntity>> getAll();

    @Query("SELECT * FROM sessions WHERE is_archived = 0 ORDER BY updated_at DESC")
    LiveData<List<SessionEntity>> getAllActive();

    @Query("SELECT * FROM sessions WHERE is_archived = 1 ORDER BY updated_at DESC")
    LiveData<List<SessionEntity>> getAllArchived();

    @Query("SELECT * FROM sessions WHERE is_pinned = 1 AND is_archived = 0 ORDER BY updated_at DESC")
    LiveData<List<SessionEntity>> getPinned();

    @Query("SELECT * FROM sessions WHERE is_archived = 0 ORDER BY updated_at DESC LIMIT :limit")
    LiveData<List<SessionEntity>> getRecent(int limit);

    @Query("SELECT * FROM sessions WHERE title LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    LiveData<List<SessionEntity>> search(String query);

    @Query("SELECT * FROM sessions WHERE created_at >= :startDate AND created_at <= :endDate ORDER BY created_at DESC")
    LiveData<List<SessionEntity>> getByDateRange(Date startDate, Date endDate);

    @Query("SELECT COUNT(*) FROM sessions")
    LiveData<Integer> getCount();

    @Query("SELECT COUNT(*) FROM sessions WHERE is_archived = 0")
    LiveData<Integer> getActiveCount();

    @Query("UPDATE sessions SET is_pinned = :isPinned WHERE id = :sessionId")
    void updatePinned(String sessionId, boolean isPinned);

    @Query("UPDATE sessions SET is_archived = :isArchived WHERE id = :sessionId")
    void updateArchived(String sessionId, boolean isArchived);

    @Query("UPDATE sessions SET message_count = message_count + 1, updated_at = :timestamp WHERE id = :sessionId")
    void incrementMessageCount(String sessionId, Date timestamp);

    @Query("UPDATE sessions SET model_id = :modelId, model_name = :modelName WHERE id = :sessionId")
    void updateModel(String sessionId, String modelId, String modelName);

    @Query("UPDATE sessions SET title = :title WHERE id = :sessionId")
    void updateTitle(String sessionId, String title);

    @Query("SELECT * FROM sessions ORDER BY updated_at DESC LIMIT 1")
    SessionEntity getMostRecentSync();
}
