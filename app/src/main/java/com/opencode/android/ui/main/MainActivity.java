package com.opencode.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import com.opencode.android.OpenCodeApplication;
import com.opencode.android.R;
import com.opencode.android.databinding.ActivityMainBinding;
import com.opencode.android.di.AppContainer;
import com.opencode.android.ui.chat.ChatViewModel;
import com.opencode.android.ui.editor.CodeEditorActivity;
import com.opencode.android.ui.models.ModelSelectionActivity;
import com.opencode.android.ui.session.SessionDetailActivity;
import com.opencode.android.ui.settings.SettingsActivity;

/**
 * Main Activity for OpenCode Android.
 * Hosts navigation and main content area.
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        NavigationBarView.OnItemSelectedListener,
        SessionAdapter.SessionClickListener {

    private ActivityMainBinding binding;
    private ChatViewModel viewModel;
    private SessionAdapter sessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ensure app is initialized before creating ViewModel
        ensureAppInitialized();

        // Initialize ViewModel with manual dependency injection
        try {
            AppContainer appContainer = OpenCodeApplication.getAppContainer();
            if (appContainer != null && appContainer.getChatRepository() != null && appContainer.getModelRepository() != null) {
                viewModel = new ViewModelProvider(this, new ChatViewModel.Factory(
                        appContainer.getChatRepository(),
                        appContainer.getModelRepository()
                )).get(ChatViewModel.class);
            } else {
                showError("Failed to initialize app. Please restart.");
                return;
            }
        } catch (Exception e) {
            showError("Initialization error: " + e.getMessage());
            return;
        }

        setupToolbar();
        setupNavigation();
        setupFab();
        setupRecyclerView();
        setupBottomNavigation();
        observeViewModel();

        // Check if launched from deep link
        handleIntent(getIntent());
    }

    private void ensureAppInitialized() {
        // Force application initialization if not already done
        OpenCodeApplication app = OpenCodeApplication.getInstance();
        if (app == null) {
            showError("Application not initialized");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener(this);

        // Update header with user info if available
        // binding.navigationView.getHeaderView(0).setUserInfo(...);
    }

    private void setupFab() {
        binding.fabNewChat.setOnClickListener(v -> {
            startNewChat();
        });
    }

    private void setupRecyclerView() {
        sessionAdapter = new SessionAdapter(this);

        binding.recyclerViewSessions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSessions.setAdapter(sessionAdapter);

        // Setup swipe to refresh
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.fetchModels();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(this);

        // Select chat tab by default
        binding.bottomNavigation.setSelectedItemId(R.id.nav_chat);
    }

    private void observeViewModel() {
        // Observe sessions
        viewModel.getAllSessions().observe(this, sessions -> {
            if (sessions != null && !sessions.isEmpty()) {
                sessionAdapter.submitList(sessions);
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.recyclerViewSessions.setVisibility(View.VISIBLE);
            } else {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewSessions.setVisibility(View.GONE);
            }
        });

        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });

        // Observe models
        viewModel.getModels().observe(this, models -> {
            // Update model selector if needed
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            // Handle deep link
            String host = intent.getData().getHost();
            if ("opencode-android".equals(host)) {
                // Handle specific deep link actions
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Bottom Navigation items
        if (itemId == R.id.nav_chat) {
            showChatScreen();
            return true;
        } else if (itemId == R.id.nav_sessions) {
            showSessionsScreen();
            return true;
        } else if (itemId == R.id.nav_projects) {
            showProjectsScreen();
            return true;
        } else if (itemId == R.id.nav_settings) {
            showSettingsScreen();
            return true;
        }
        // Drawer Navigation items
        else if (itemId == R.id.nav_new_chat) {
            startNewChat();
            return true;
        } else if (itemId == R.id.nav_select_model) {
            selectModel();
            return true;
        }

        return false;
    }

    private void showChatScreen() {
        // Show chat fragment
        binding.toolbar.setTitle(R.string.chat_title);
        // getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChatFragment()).commit();
    }

    private void showSessionsScreen() {
        binding.toolbar.setTitle(R.string.sessions_title);
    }

    private void showProjectsScreen() {
        binding.toolbar.setTitle(R.string.nav_projects);
    }

    private void showSettingsScreen() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startNewChat() {
        try {
            // Check if ViewModel is initialized
            if (viewModel == null) {
                showError("App not ready. Please wait...");
                return;
            }

            String modelId = viewModel.getDefaultModel();
            // Use a valid free model ID if no default is set
            if (modelId == null || modelId.isEmpty()) {
                modelId = "opencode/sonic";
            }

            // Create session and get session ID directly
            String sessionId = viewModel.createSession("New Chat", modelId);

            // Validate session ID
            if (sessionId == null || sessionId.isEmpty()) {
                showError("Failed to create session");
                return;
            }

            // Navigate to new chat
            Intent intent = new Intent(this, SessionDetailActivity.class);
            intent.putExtra(SessionDetailActivity.EXTRA_SESSION_ID, sessionId);
            startActivity(intent);
        } catch (Exception e) {
            showError("Error creating chat: " + e.getMessage());
        }
    }

    private void selectModel() {
        Intent intent = new Intent(this, ModelSelectionActivity.class);
        startActivity(intent);
    }

    private void openEditor(String filePath) {
        Intent intent = new Intent(this, CodeEditorActivity.class);
        intent.putExtra(CodeEditorActivity.EXTRA_FILE_PATH, filePath);
        startActivity(intent);
    }

    private void showError(String message) {
        com.google.android.material.snackbar.Snackbar.make(
                binding.getRoot(),
                message,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show();
    }

    @Override
    public void onSessionClick(com.opencode.android.data.local.entity.SessionEntity session) {
        Intent intent = new Intent(this, SessionDetailActivity.class);
        intent.putExtra(SessionDetailActivity.EXTRA_SESSION_ID, session.getId());
        startActivity(intent);
    }

    @Override
    public void onSessionLongClick(com.opencode.android.data.local.entity.SessionEntity session) {
        showSessionOptions(session);
    }

    private void showSessionOptions(com.opencode.android.data.local.entity.SessionEntity session) {
        String[] options = {
                getString(R.string.action_edit),
                getString(R.string.action_pin),
                getString(R.string.action_delete)
        };

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(session.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            // Rename session
                            break;
                        case 1: // Pin
                            viewModel.pinSession(session.getId(), !session.isPinned());
                            break;
                        case 2: // Delete
                            confirmDeleteSession(session);
                            break;
                    }
                })
                .show();
    }

    private void confirmDeleteSession(com.opencode.android.data.local.entity.SessionEntity session) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_delete)
                .setMessage(R.string.session_delete_confirm)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteSession(session.getId());
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
