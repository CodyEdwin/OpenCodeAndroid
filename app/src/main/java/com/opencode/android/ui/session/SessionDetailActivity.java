package com.opencode.android.ui.session;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(SessionDetailViewModel.class);

        setupRecyclerView();
        loadSession();
    }

    private void setupRecyclerView() {
        binding.recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        // Message adapter would be set here
    }

    private void loadSession() {
        if (sessionId != null) {
            viewModel.loadSession(sessionId);
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
