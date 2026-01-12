package com.opencode.android.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.opencode.android.data.local.entity.MessageEntity;

import java.util.List;

/**
 * Data Access Object for Message operations.
 */
@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MessageEntity> messages);

    @Update
    void update(MessageEntity message);

    @Delete
    void delete(MessageEntity message);

    @Query("DELETE FROM messages WHERE id = :messageId")
    void deleteById(String messageId);

    @Query("DELETE FROM messages WHERE session_id = :sessionId")
    void deleteBySessionId(String sessionId);

    @Query("DELETE FROM messages")
    void deleteAll();

    @Query("SELECT * FROM messages WHERE id = :messageId")
    LiveData<MessageEntity> getById(String messageId);

    @Query("SELECT * FROM messages WHERE id = :messageId")
    MessageEntity getByIdSync(String messageId);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY created_at ASC")
    LiveData<List<MessageEntity>> getBySessionId(String sessionId);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY created_at ASC")
    List<MessageEntity> getBySessionIdSync(String sessionId);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<MessageEntity>> getRecentBySessionId(String sessionId, int limit);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY created_at DESC LIMIT 1")
    LiveData<MessageEntity> getLastBySessionId(String sessionId);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY created_at DESC LIMIT 1")
    MessageEntity getLastBySessionIdSync(String sessionId);

    @Query("SELECT COUNT(*) FROM messages WHERE session_id = :sessionId")
    LiveData<Integer> getCountBySessionId(String sessionId);

    @Query("SELECT COUNT(*) FROM messages WHERE session_id = :sessionId")
    int getCountBySessionIdSync(String sessionId);

    @Query("SELECT SUM(token_count) FROM messages WHERE session_id = :sessionId")
    LiveData<Integer> getTotalTokensBySessionId(String sessionId);

    @Query("SELECT * FROM messages WHERE role = :role ORDER BY created_at DESC")
    LiveData<List<MessageEntity>> getByRole(String role);

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY created_at DESC")
    LiveData<List<MessageEntity>> search(String query);

    @Query("UPDATE messages SET content = :content WHERE id = :messageId")
    void updateContent(String messageId, String content);

    @Query("UPDATE messages SET is_complete = :isComplete WHERE id = :messageId")
    void updateComplete(String messageId, boolean isComplete);

    @Query("UPDATE messages SET token_count = :tokenCount WHERE id = :messageId")
    void updateTokenCount(String messageId, int tokenCount);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId AND role = 'assistant' ORDER BY created_at DESC LIMIT 1")
    MessageEntity getLastAssistantMessageSync(String sessionId);
}
