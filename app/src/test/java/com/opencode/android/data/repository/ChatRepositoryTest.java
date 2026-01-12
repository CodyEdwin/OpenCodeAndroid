package com.opencode.android.data.repository;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.opencode.android.data.local.dao.MessageDao;
import com.opencode.android.data.local.dao.SessionDao;
import com.opencode.android.data.local.entity.MessageEntity;
import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.data.remote.zen.ZenApiService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ChatRepository.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChatRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private SessionDao sessionDao;

    @Mock
    private MessageDao messageDao;

    @Mock
    private ZenApiService apiService;

    @Mock
    private ExecutorService executorService;

    private ChatRepositoryImpl chatRepository;

    @Before
    public void setup() {
        chatRepository = new ChatRepositoryImpl(sessionDao, messageDao, apiService, executorService);
    }

    @Test
    public void createSession_shouldCreateNewSession() {
        // Arrange
        String title = "Test Session";
        String modelId = "gpt-5.1-codex";

        // Act
        SessionEntity session = chatRepository.createSession(title, modelId);

        // Assert
        assertNotNull(session);
        assertEquals(title, session.getTitle());
        assertEquals(modelId, session.getModelId());
        verify(sessionDao).insert(any(SessionEntity.class));
    }

    @Test
    public void getAllSessions_shouldReturnAllSessions() {
        // Arrange
        List<SessionEntity> expectedSessions = Arrays.asList(
            new SessionEntity("Session 1", "model-1"),
            new SessionEntity("Session 2", "model-2")
        );
        MutableLiveData<List<SessionEntity>> liveData = new MutableLiveData<>(expectedSessions);
        when(sessionDao.getAllActive()).thenReturn(liveData);

        // Act
        LiveData<List<SessionEntity>> result = chatRepository.getAllSessions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getValue().size());
    }

    @Test
    public void getMessagesBySessionId_shouldReturnMessages() {
        // Arrange
        String sessionId = "test-session-id";
        List<MessageEntity> expectedMessages = Arrays.asList(
            MessageEntity.user(sessionId, "Hello"),
            MessageEntity.assistant(sessionId, "Hi there!")
        );
        MutableLiveData<List<MessageEntity>> liveData = new MutableLiveData<>(expectedMessages);
        when(messageDao.getBySessionId(sessionId)).thenReturn(liveData);

        // Act
        LiveData<List<MessageEntity>> result = chatRepository.getMessagesBySessionId(sessionId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getValue().size());
    }

    @Test
    public void deleteSession_shouldDeleteSessionAndMessages() {
        // Arrange
        String sessionId = "test-session-id";

        // Act
        chatRepository.deleteSession(sessionId);

        // Assert
        verify(sessionDao).deleteById(sessionId);
        verify(messageDao).deleteBySessionId(sessionId);
    }

    @Test
    public void pinSession_shouldUpdatePinnedStatus() {
        // Arrange
        String sessionId = "test-session-id";
        boolean pinned = true;

        // Act
        chatRepository.pinSession(sessionId, pinned);

        // Assert
        verify(sessionDao).updatePinned(sessionId, pinned);
    }

    @Test
    public void archiveSession_shouldUpdateArchivedStatus() {
        // Arrange
        String sessionId = "test-session-id";
        boolean archived = true;

        // Act
        chatRepository.archiveSession(sessionId, archived);

        // Assert
        verify(sessionDao).updateArchived(sessionId, archived);
    }
}
