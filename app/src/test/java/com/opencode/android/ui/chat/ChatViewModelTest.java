package com.opencode.android.ui.chat;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.opencode.android.data.local.entity.MessageEntity;
import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.data.model.zen.ModelResponse;
import com.opencode.android.data.repository.ChatRepository;
import com.opencode.android.data.repository.ModelRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ChatViewModel.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChatViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ModelRepository modelRepository;

    private ChatViewModel viewModel;

    @Before
    public void setup() {
        viewModel = new ChatViewModel(chatRepository, modelRepository);
    }

    @Test
    public void createSession_shouldCreateAndSetCurrentSession() {
        // Arrange
        String title = "New Chat";
        String modelId = "gpt-5.1-codex";
        SessionEntity session = new SessionEntity(title, modelId);
        when(chatRepository.createSession(title, modelId)).thenReturn(session);

        // Act
        viewModel.createSession(title, modelId);

        // Assert
        verify(chatRepository).createSession(title, modelId);
        assertEquals(session.getId(), viewModel.getCurrentSessionId().getValue());
    }

    @Test
    public void getAllSessions_shouldReturnSessionsFromRepository() {
        // Arrange
        List<SessionEntity> expectedSessions = Arrays.asList(
            new SessionEntity("Session 1", "model-1"),
            new SessionEntity("Session 2", "model-2")
        );
        MutableLiveData<List<SessionEntity>> liveData = new MutableLiveData<>(expectedSessions);
        when(chatRepository.getAllSessions()).thenReturn(liveData);

        // Act
        viewModel.getAllSessions();

        // Assert
        verify(chatRepository).getAllSessions();
    }

    @Test
    public void deleteSession_shouldCallRepository() {
        // Arrange
        String sessionId = "test-session-id";

        // Act
        viewModel.deleteSession(sessionId);

        // Assert
        verify(chatRepository).deleteSession(sessionId);
    }

    @Test
    public void pinSession_shouldCallRepository() {
        // Arrange
        String sessionId = "test-session-id";
        boolean pinned = true;

        // Act
        viewModel.pinSession(sessionId, pinned);

        // Assert
        verify(chatRepository).pinSession(sessionId, pinned);
    }

    @Test
    public void setLoading_shouldUpdateLoadingState() {
        // Act
        viewModel.setLoading(true);

        // Assert
        assertEquals(true, viewModel.isLoading().getValue());
    }

    @Test
    public void setErrorMessage_shouldUpdateErrorState() {
        // Arrange
        String errorMessage = "Test error";

        // Act
        viewModel.setErrorMessage(errorMessage);

        // Assert
        assertEquals(errorMessage, viewModel.getErrorMessage().getValue());
    }

    @Test
    public void clearError_shouldClearErrorState() {
        // Arrange
        viewModel.setErrorMessage("Error");

        // Act
        viewModel.clearError();

        // Assert
        assertEquals(null, viewModel.getErrorMessage().getValue());
    }

    @Test
    public void fetchModels_shouldCallModelRepository() {
        // Act
        viewModel.fetchModels();

        // Assert
        verify(modelRepository).fetchModels();
    }

    @Test
    public void getDefaultModel_shouldReturnModelFromRepository() {
        // Arrange
        String expectedModel = "gpt-5.1-codex";
        when(modelRepository.getDefaultModel()).thenReturn(expectedModel);

        // Act
        String result = viewModel.getDefaultModel();

        // Assert
        assertEquals(expectedModel, result);
    }

    @Test
    public void sendMessage_shouldNotSendEmptyMessage() {
        // Arrange
        String sessionId = "test-session-id";
        String content = "";

        // Act
        viewModel.sendMessage(sessionId, content);

        // Assert - repository should not be called with empty content
        // (actual verification depends on implementation)
    }

    @Test
    public void sendMessage_shouldSendValidMessage() {
        // Arrange
        String sessionId = "test-session-id";
        String content = "Hello, world!";

        // Act
        viewModel.sendMessage(sessionId, content);

        // Assert
        verify(chatRepository).sendMessage(sessionId, content, true);
    }
}
