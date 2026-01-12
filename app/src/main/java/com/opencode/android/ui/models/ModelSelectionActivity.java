package com.opencode.android.ui.models;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.opencode.android.R;
import com.opencode.android.databinding.ActivityModelSelectionBinding;

/**
 * Activity for selecting AI models.
 * Displays a list of available models from different providers.
 */
public class ModelSelectionActivity extends AppCompatActivity {

    private ActivityModelSelectionBinding binding;
    private ModelSelectionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModelSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.models_title);
        }

        viewModel = new ViewModelProvider(this).get(ModelSelectionViewModel.class);

        setupRecyclerView();
        observeModels();
    }

    private void setupRecyclerView() {
        binding.recyclerViewModels.setLayoutManager(new LinearLayoutManager(this));
        // Adapter would be set here
    }

    private void observeModels() {
        // Observe model list from ViewModel
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
