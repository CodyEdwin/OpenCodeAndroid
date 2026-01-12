package com.opencode.android.ui.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.opencode.android.data.local.entity.MessageEntity;
import com.opencode.android.data.local.entity.SessionEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for session detail screen.
 */
public class SessionDetailViewModel extends ViewModel {

    private final MutableLiveData<SessionEntity> session = new MutableLiveData<>();
    private final MutableLiveData<List<MessageEntity>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<SessionEntity> getSession() {
        return session;
    }

    public LiveData<List<MessageEntity>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void loadSession(String sessionId) {
        isLoading.setValue(true);
        // Load session and messages from repository
        // This would be implemented with actual data loading
    }

    public void sendMessage(String content) {
        // Send message and add to list
    }

    public void deleteSession() {
        // Delete current session
    }
}
