package com.opencode.android.data.repository;

import androidx.lifecycle.LiveData;

import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.data.local.entity.MessageEntity;

import java.util.List;

/**
 * Repository interface for chat operations.
 */
public interface ChatRepository {

    // Session operations
    LiveData<List<SessionEntity>> getAllSessions();
    LiveData<List<SessionEntity>> getRecentSessions(int limit);
    LiveData<SessionEntity> getSessionById(String sessionId);
    SessionEntity createSession(String title, String modelId);
    void updateSession(SessionEntity session);
    void deleteSession(String sessionId);
    void deleteAllSessions();
    void pinSession(String sessionId, boolean pinned);
    void archiveSession(String sessionId, boolean archived);
    LiveData<List<SessionEntity>> searchSessions(String query);

    // Message operations
    LiveData<List<MessageEntity>> getMessagesBySessionId(String sessionId);
    LiveData<MessageEntity> getMessageById(String messageId);
    void saveMessage(MessageEntity message);
    void updateMessage(MessageEntity message);
    void deleteMessage(String messageId);
    void deleteAllMessagesBySessionId(String sessionId);
    LiveData<Integer> getMessageCount(String sessionId);
    LiveData<Integer> getTotalTokens(String sessionId);
    LiveData<List<MessageEntity>> searchMessages(String query);

    // Send message
    void sendMessage(String sessionId, String content, boolean streaming);
    void sendMessageWithHistory(String sessionId, List<MessageEntity> history, boolean streaming);
}
