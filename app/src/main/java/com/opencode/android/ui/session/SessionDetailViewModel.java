package com.opencode.android.ui.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.opencode.android.data.local.entity.MessageEntity;
import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.data.repository.ChatRepository;
import com.opencode.android.data.repository.ChatRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for session detail screen.
 */
public class SessionDetailViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final MutableLiveData<SessionEntity> session = new MutableLiveData<>();
    private final MutableLiveData<List<MessageEntity>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SessionDetailViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public LiveData<SessionEntity> getSession() {
        return session;
    }

    public LiveData<List<MessageEntity>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            errorMessage.setValue("Invalid session ID");
            isLoading.setValue(false);
            return;
        }

        isLoading.setValue(true);

        // Observe session from repository
        chatRepository.getSessionById(sessionId).observeForever(sessionEntity -> {
            if (sessionEntity != null) {
                session.setValue(sessionEntity);
            }
            isLoading.setValue(false);
        });

        // Observe messages from repository
        chatRepository.getMessagesBySessionId(sessionId).observeForever(messageEntities -> {
            if (messageEntities != null) {
                messages.setValue(messageEntities);
            }
        });
    }

    public void sendMessage(String content) {
        SessionEntity currentSession = session.getValue();
        if (currentSession == null || content == null || content.trim().isEmpty()) {
            return;
        }
        chatRepository.sendMessage(currentSession.getId(), content.trim(), true);
    }

    public void deleteSession() {
        SessionEntity currentSession = session.getValue();
        if (currentSession != null) {
            chatRepository.deleteSession(currentSession.getId());
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * Factory for creating SessionDetailViewModel with dependencies.
     */
    public static class Factory implements androidx.lifecycle.ViewModelProvider.Factory {
        private final ChatRepository chatRepository;

        public Factory(ChatRepository chatRepository) {
            this.chatRepository = chatRepository;
        }

        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SessionDetailViewModel.class)) {
                return (T) new SessionDetailViewModel(chatRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
