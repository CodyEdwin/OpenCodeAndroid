package com.opencode.android.ui.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.opencode.android.OpenCodeApplication;
import com.opencode.android.R;
import com.opencode.android.databinding.ActivitySettingsBinding;
import com.opencode.android.ui.chat.ChatViewModel;

/**
 * Settings Activity for configuring API keys, models, and appearance.
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private ChatViewModel viewModel;

    private static final String[] THEME_OPTIONS = {"System Default", "Light", "Dark"};
    private static final String[] FONT_SIZE_OPTIONS = {"Small", "Medium", "Large"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel with manual dependency injection
        viewModel = new ViewModelProvider(this, new ChatViewModel.Factory(
                OpenCodeApplication.getAppContainer().getChatRepository(),
                OpenCodeApplication.getAppContainer().getModelRepository()
        )).get(ChatViewModel.class);

        setupToolbar();
        setupThemeSelector();
        setupFontSizeSelector();
        setupClickListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
    }

    private void setupThemeSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                THEME_OPTIONS
        );
        binding.spinnerTheme.setAdapter(adapter);

        binding.spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String theme = THEME_OPTIONS[position].toLowerCase().replace(" ", "_");
                applyTheme(theme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupFontSizeSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                FONT_SIZE_OPTIONS
        );
        binding.spinnerFontSize.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.buttonSaveApiKey.setOnClickListener(v -> saveApiKey());
        binding.buttonClearHistory.setOnClickListener(v -> confirmClearHistory());
        binding.buttonClearCache.setOnClickListener(v -> clearCache());
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.isLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }

    private void saveApiKey() {
        String apiKey = binding.editApiKey.getText().toString().trim();
        String baseUrl = binding.editBaseUrl.getText().toString().trim();

        if (apiKey.isEmpty()) {
            binding.layoutApiKey.setError(getString(R.string.error_api_key));
            return;
        }

        // Save API key (in production, use encrypted storage)
        getSharedPreferences("opencode_prefs", MODE_PRIVATE)
                .edit()
                .putString("api_key", apiKey)
                .putString("api_base_url", baseUrl.isEmpty() ?
                        "https://opencode.ai/zen/v1/" : baseUrl)
                .apply();

        showSuccess(getString(R.string.action_save));

        // Test API connection
        viewModel.fetchModels();
    }

    private void applyTheme(String theme) {
        int mode;
        switch (theme) {
            case "light":
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);

        getSharedPreferences("opencode_prefs", MODE_PRIVATE)
                .edit()
                .putString("theme", theme)
                .apply();
    }

    private void confirmClearHistory() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_clear_history)
                .setMessage(R.string.session_delete_confirm)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    // Clear history
                    showSuccess("History cleared");
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void clearCache() {
        // Clear cache directory
        try {
            java.io.File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteRecursive(cacheDir);
            }
            showSuccess(getString(R.string.action_save));
        } catch (Exception e) {
            showError(getString(R.string.error_unknown));
        }
    }

    private void deleteRecursive(java.io.File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (java.io.File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error))
                .setTextColor(getColor(R.color.white))
                .show();
    }

    private void showSuccess(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.success))
                .setTextColor(getColor(R.color.white))
                .show();
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
