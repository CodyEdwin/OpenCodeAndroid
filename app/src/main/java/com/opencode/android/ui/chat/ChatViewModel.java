package com.opencode.android.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.opencode.android.data.local.entity.MessageEntity;
import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.data.repository.ChatRepository;
import com.opencode.android.data.repository.ModelRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Chat functionality.
 */
public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final ModelRepository modelRepository;

    private final MutableLiveData<String> currentSessionId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isStreaming = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> tokenCount = new MutableLiveData<>(0);

    /**
     * Constructor with manual dependency injection.
     */
    public ChatViewModel(ChatRepository chatRepository, ModelRepository modelRepository) {
        this.chatRepository = chatRepository;
        this.modelRepository = modelRepository;
    }

    // Session operations
    public LiveData<List<SessionEntity>> getAllSessions() {
        return chatRepository.getAllSessions();
    }

    public LiveData<SessionEntity> getSession(String sessionId) {
        return chatRepository.getSessionById(sessionId);
    }

    public String createSession(String title, String modelId) {
        SessionEntity session = chatRepository.createSession(title, modelId);
        currentSessionId.setValue(session.getId());
        return session.getId();
    }

    public void deleteSession(String sessionId) {
        chatRepository.deleteSession(sessionId);
    }

    public void pinSession(String sessionId, boolean pinned) {
        chatRepository.pinSession(sessionId, pinned);
    }

    public void archiveSession(String sessionId, boolean archived) {
        chatRepository.archiveSession(sessionId, archived);
    }

    public LiveData<List<SessionEntity>> searchSessions(String query) {
        return chatRepository.searchSessions(query);
    }

    // Message operations
    public LiveData<List<MessageEntity>> getMessages(String sessionId) {
        return chatRepository.getMessagesBySessionId(sessionId);
    }

    public void sendMessage(String sessionId, String content) {
        if (sessionId == null || content == null || content.trim().isEmpty()) {
            return;
        }

        isLoading.setValue(true);
        isStreaming.setValue(true);

        // Save user message
        MessageEntity userMessage = MessageEntity.user(sessionId, content.trim());
        chatRepository.saveMessage(userMessage);

        // Get history and send to API
        List<MessageEntity> history = new ArrayList<>();
        LiveData<List<MessageEntity>> messagesLiveData = chatRepository.getMessagesBySessionId(sessionId);

        // Note: In production, properly observe and collect history
        chatRepository.sendMessage(sessionId, content, true);
    }

    public void sendMessageWithHistory(String sessionId, List<MessageEntity> history) {
        isLoading.setValue(true);
        chatRepository.sendMessageWithHistory(sessionId, history, true);
    }

    // Session management
    public void setCurrentSessionId(String sessionId) {
        currentSessionId.setValue(sessionId);
    }

    public LiveData<String> getCurrentSessionId() {
        return currentSessionId;
    }

    // Loading state
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    // Error handling
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        errorMessage.setValue(message);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    // Streaming state
    public LiveData<Boolean> isStreaming() {
        return isStreaming;
    }

    public void setStreaming(boolean streaming) {
        isStreaming.setValue(streaming);
        if (!streaming) {
            isLoading.setValue(false);
        }
    }

    // Token count
    public LiveData<Integer> getTokenCount(String sessionId) {
        return chatRepository.getTotalTokens(sessionId);
    }

    public void setTokenCount(int count) {
        tokenCount.setValue(count);
    }

    // Model operations
    public LiveData<List<com.opencode.android.data.model.zen.ModelResponse.ModelInfo>> getModels() {
        return modelRepository.getModels();
    }

    public void fetchModels() {
        modelRepository.fetchModels();
    }

    public LiveData<Boolean> modelsLoading() {
        return modelRepository.isLoading();
    }

    public LiveData<String> modelsError() {
        return modelRepository.getError();
    }

    public String getDefaultModel() {
        return modelRepository.getDefaultModel();
    }

    public void setDefaultModel(String modelId) {
        modelRepository.setDefaultModel(modelId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        modelRepository.cleanup();
    }

    /**
     * Factory for creating ChatViewModel with dependencies.
     */
    public static class Factory implements androidx.lifecycle.ViewModelProvider.Factory {
        private final ChatRepository chatRepository;
        private final ModelRepository modelRepository;

        public Factory(ChatRepository chatRepository, ModelRepository modelRepository) {
            this.chatRepository = chatRepository;
            this.modelRepository = modelRepository;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ChatViewModel.class)) {
                return (T) new ChatViewModel(chatRepository, modelRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
