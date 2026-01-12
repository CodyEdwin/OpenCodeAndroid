package com.opencode.android.ui.session;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.opencode.android.OpenCodeApplication;
import com.opencode.android.R;
import com.opencode.android.databinding.ActivitySessionDetailBinding;

/**
 * Activity for displaying session details.
 * Shows messages and allows interaction within a chat session.
 */
public class SessionDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "session_id";

    private ActivitySessionDetailBinding binding;
    private SessionDetailViewModel viewModel;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySessionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);

        // Validate session ID
        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, R.string.error_invalid_session, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel with dependency injection
        viewModel = new ViewModelProvider(this, new SessionDetailViewModel.Factory(
                OpenCodeApplication.getAppContainer().getChatRepository()
        )).get(SessionDetailViewModel.class);

        setupToolbar();
        setupRecyclerView();
        loadSession();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.session_detail_title);
        }
    }

    private void setupRecyclerView() {
        binding.recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        // Message adapter would be set here
    }

    private void loadSession() {
        if (sessionId != null) {
            viewModel.loadSession(sessionId);

            // Observe session for toolbar title
            viewModel.getSession().observe(this, session -> {
                if (session != null && getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(session.getTitle());
                }
            });

            // Observe errors
            viewModel.getErrorMessage().observe(this, error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
