package com.opencode.android.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.opencode.android.data.local.dao.MessageDao;
import com.opencode.android.data.local.dao.SessionDao;
import com.opencode.android.data.local.entity.MessageEntity;
import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.data.model.zen.ChatMessage;
import com.opencode.android.data.model.zen.ChatRequest;
import com.opencode.android.data.model.zen.ChatResponse;
import com.opencode.android.data.remote.zen.ZenApiService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Implementation of ChatRepository.
 * Handles chat operations with OpenCode Zen API.
 */
public class ChatRepositoryImpl implements ChatRepository {

    private static final String TAG = "ChatRepository";

    private final SessionDao sessionDao;
    private final MessageDao messageDao;
    private final ZenApiService apiService;
    private final ExecutorService executorService;
    private final Supplier<String> authTokenProvider;
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Constructor with manual dependency injection.
     *
     * @param sessionDao       Session DAO
     * @param messageDao       Message DAO
     * @param apiService       Zen API service
     * @param executorService  Executor for background operations
     * @param authTokenProvider Supplier for authentication tokens
     */
    public ChatRepositoryImpl(
            SessionDao sessionDao,
            MessageDao messageDao,
            ZenApiService apiService,
            ExecutorService executorService,
            Supplier<String> authTokenProvider) {
        this.sessionDao = sessionDao;
        this.messageDao = messageDao;
        this.apiService = apiService;
        this.executorService = executorService;
        this.authTokenProvider = authTokenProvider;
    }

    /**
     * Get the current authentication token.
     */
    private String getAuthToken() {
        return authTokenProvider != null ? authTokenProvider.get() : null;
    }

    // Session operations
    @Override
    public LiveData<List<SessionEntity>> getAllSessions() {
        return sessionDao.getAllActive();
    }

    @Override
    public LiveData<List<SessionEntity>> getRecentSessions(int limit) {
        return sessionDao.getRecent(limit);
    }

    @Override
    public LiveData<SessionEntity> getSessionById(String sessionId) {
        return sessionDao.getById(sessionId);
    }

    @Override
    public SessionEntity createSession(String title, String modelId) {
        SessionEntity session = new SessionEntity(title, modelId);
        sessionDao.insert(session);
        return session;
    }

    @Override
    public void updateSession(SessionEntity session) {
        session.updateTimestamp();
        sessionDao.update(session);
    }

    @Override
    public void deleteSession(String sessionId) {
        executorService.execute(() -> {
            sessionDao.deleteById(sessionId);
            messageDao.deleteBySessionId(sessionId);
        });
    }

    @Override
    public void deleteAllSessions() {
        executorService.execute(() -> {
            sessionDao.deleteAll();
            messageDao.deleteAll();
        });
    }

    @Override
    public void pinSession(String sessionId, boolean pinned) {
        sessionDao.updatePinned(sessionId, pinned);
    }

    @Override
    public void archiveSession(String sessionId, boolean archived) {
        sessionDao.updateArchived(sessionId, archived);
    }

    @Override
    public LiveData<List<SessionEntity>> searchSessions(String query) {
        return sessionDao.search(query);
    }

    // Message operations
    @Override
    public LiveData<List<MessageEntity>> getMessagesBySessionId(String sessionId) {
        return messageDao.getBySessionId(sessionId);
    }

    @Override
    public LiveData<MessageEntity> getMessageById(String messageId) {
        return messageDao.getById(messageId);
    }

    @Override
    public void saveMessage(MessageEntity message) {
        executorService.execute(() -> {
            messageDao.insert(message);
            sessionDao.incrementMessageCount(message.getSessionId(), new Date());
        });
    }

    @Override
    public void updateMessage(MessageEntity message) {
        executorService.execute(() -> messageDao.update(message));
    }

    @Override
    public void deleteMessage(String messageId) {
        executorService.execute(() -> messageDao.deleteById(messageId));
    }

    @Override
    public void deleteAllMessagesBySessionId(String sessionId) {
        executorService.execute(() -> messageDao.deleteBySessionId(sessionId));
    }

    @Override
    public LiveData<Integer> getMessageCount(String sessionId) {
        return messageDao.getCountBySessionId(sessionId);
    }

    @Override
    public LiveData<Integer> getTotalTokens(String sessionId) {
        return messageDao.getTotalTokensBySessionId(sessionId);
    }

    @Override
    public LiveData<List<MessageEntity>> searchMessages(String query) {
        return messageDao.search(query);
    }

    // Send message
    @Override
    public void sendMessage(String sessionId, String content, boolean streaming) {
        executorService.execute(() -> {
            // Save user message
            MessageEntity userMessage = MessageEntity.user(sessionId, content);
            messageDao.insert(userMessage);

            // Create assistant message placeholder
            MessageEntity assistantMessage = new MessageEntity(sessionId, "assistant", "");
            messageDao.insert(assistantMessage);

            // Update session timestamp
            sessionDao.incrementMessageCount(sessionId, new Date());
        });
    }

    @Override
    public void sendMessageWithHistory(String sessionId, List<MessageEntity> history, boolean streaming) {
        // Convert entities to API messages
        List<ChatMessage> messages = new ArrayList<>();
        for (MessageEntity entity : history) {
            messages.add(new ChatMessage(entity.getRole(), entity.getContent()));
        }

        // Get session for model info
        SessionEntity session = sessionDao.getByIdSync(sessionId);
        if (session == null) {
            Log.e(TAG, "Session not found: " + sessionId);
            return;
        }

        // Create API request
        ChatRequest request = new ChatRequest.Builder()
                .model(session.getModelId())
                .messages(messages)
                .stream(streaming)
                .build();

        if (streaming) {
            sendStreamingMessage(sessionId, request);
        } else {
            sendNonStreamingMessage(sessionId, request);
        }
    }

    private void sendNonStreamingMessage(String sessionId, ChatRequest request) {
        String authToken = getAuthToken();
        if (authToken == null) {
            Log.e(TAG, "No authentication token available");
            return;
        }

        apiService.createCompletion(authToken, request).enqueue(new retrofit2.Callback<ChatResponse>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<ChatResponse> call,
                                   @androidx.annotation.NonNull retrofit2.Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                    response.body().getFirstMessageContent() != null) {
                    // Save assistant response
                    MessageEntity assistantMessage = new MessageEntity(
                            sessionId, "assistant", response.body().getFirstMessageContent()
                    );
                    messageDao.insert(assistantMessage);
                    sessionDao.incrementMessageCount(sessionId, new Date());
                } else if (response.code() == 401) {
                    Log.e(TAG, "Authentication failed - invalid API key");
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<ChatResponse> call,
                                  @androidx.annotation.NonNull Throwable t) {
                Log.e(TAG, "Error sending message to OpenCode Zen API", t);
            }
        });
    }

    private void sendStreamingMessage(String sessionId, ChatRequest request) {
        String authToken = getAuthToken();
        if (authToken == null) {
            Log.e(TAG, "No authentication token available for streaming");
            return;
        }

        MutableLiveData<String> streamedContent = new MutableLiveData<>("");
        MessageEntity assistantMessage = new MessageEntity(sessionId, "assistant", "");

        // For now, use non-streaming as streaming implementation requires SSE parsing
        // This can be enhanced later with proper SSE event parsing
        apiService.createCompletion(authToken, request).enqueue(new retrofit2.Callback<ChatResponse>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<ChatResponse> call,
                                   @androidx.annotation.NonNull retrofit2.Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                    response.body().getFirstMessageContent() != null) {
                    assistantMessage.setContent(response.body().getFirstMessageContent());
                    messageDao.insert(assistantMessage);
                    sessionDao.incrementMessageCount(sessionId, new Date());
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<ChatResponse> call,
                                  @androidx.annotation.NonNull Throwable t) {
                Log.e(TAG, "Error in streaming", t);
            }
        });
    }

    /**
     * Clean up resources.
     */
    public void cleanup() {
        disposables.clear();
    }
}
