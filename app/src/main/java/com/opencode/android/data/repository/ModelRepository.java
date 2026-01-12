package com.opencode.android.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.opencode.android.data.local.dao.SettingsDao;
import com.opencode.android.data.local.entity.SettingsEntity;
import com.opencode.android.data.model.zen.ModelResponse;
import com.opencode.android.data.remote.zen.ZenApiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Repository for managing AI models.
 * Fetches models from OpenCode Zen API and provides built-in free models.
 */
public class ModelRepository {

    private static final String TAG = "ModelRepository";
    private static final String CACHE_KEY = "models_cache";
    private static final long CACHE_DURATION_MS = 60 * 60 * 1000; // 1 hour

    // Built-in free models that work without API key authentication
    private static final List<FreeModel> BUILTIN_FREE_MODELS = Arrays.asList(
            new FreeModel("sonic", "Sonic", "OpenCode", "Fast and lightweight coding assistant", true, false),
            new FreeModel("sonic-code", "Sonic Code", "OpenCode", "Code-optimized Sonic variant", true, true),
            new FreeModel("grok-code-fast", "Grok Code Fast", "xAI", "Fast coding assistance from Grok", false, true),
            new FreeModel("big-pickle", "Big Pickle", "OpenCode", "High-capability general purpose model", false, true),
            new FreeModel("minimax-m2.1", "MiniMax M2.1", "MiniMax", "Efficient multi-purpose model", false, true),
            new FreeModel("glm-4.7", "GLM 4.7", "Zhipu AI", "Advanced Chinese-English bilingual model", false, true),
            new FreeModel("gpt-5-nano", "GPT-5 Nano", "OpenAI", "Compact GPT-5 variant for efficiency", false, true)
    );

    private final ZenApiService apiService;
    private final SettingsDao settingsDao;
    private final ExecutorService executorService;
    private final Supplier<String> authTokenProvider;
    private boolean isFreeModeEnabled = false;

    private final MutableLiveData<List<ModelResponse.ModelInfo>> modelsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    /**
     * Represents a built-in free model.
     */
    public static class FreeModel {
        private final String id;
        private final String displayName;
        private final String provider;
        private final String description;
        private final boolean supportsStreaming;
        private final boolean supportsTools;

        public FreeModel(String id, String displayName, String provider,
                        String description, boolean supportsStreaming, boolean supportsTools) {
            this.id = id;
            this.displayName = displayName;
            this.provider = provider;
            this.description = description;
            this.supportsStreaming = supportsStreaming;
            this.supportsTools = supportsTools;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getProvider() { return provider; }
        public String getDescription() { return description; }
        public boolean supportsStreaming() { return supportsStreaming; }
        public boolean supportsTools() { return supportsTools; }

        /**
         * Convert FreeModel to ModelInfo for compatibility.
         */
        public ModelResponse.ModelInfo toModelInfo() {
            ModelResponse.ModelInfo info = new ModelResponse.ModelInfo();
            info.setId("opencode/" + id);
            info.setObject("model");
            info.setCreated(System.currentTimeMillis() / 1000);
            info.setOwnedBy(provider);

            // Set capabilities
            Map<String, Boolean> capabilities = new HashMap<>();
            capabilities.put("stream", supportsStreaming);
            capabilities.put("tools", supportsTools);
            capabilities.put("vision", false);
            capabilities.put("parallel_tool_calls", supportsTools);
            info.setCapabilities(capabilities);

            // Set free pricing
            ModelResponse.ModelInfo.Pricing pricing = new ModelResponse.ModelInfo.Pricing();
            pricing.setPromptTokens("$0.00");
            pricing.setCompletionTokens("$0.00");
            pricing.setImageTokens("$0.00");
            pricing.setRequest("$0.00");
            info.setPricing(pricing);

            return info;
        }
    }

    /**
     * Constructor with manual dependency injection.
     *
     * @param apiService        The Zen API service
     * @param settingsDao       Settings DAO for caching
     * @param executorService   Executor for background operations
     * @param authTokenProvider Supplier for authentication tokens
     */
    public ModelRepository(
            ZenApiService apiService,
            SettingsDao settingsDao,
            ExecutorService executorService,
            Supplier<String> authTokenProvider) {
        this.apiService = apiService;
        this.settingsDao = settingsDao;
        this.executorService = executorService;
        this.authTokenProvider = authTokenProvider;
    }

    // Public API methods for ViewModels

    public LiveData<List<ModelResponse.ModelInfo>> getModels() {
        return modelsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    // Model management methods

    public boolean isAuthenticated() {
        return getAuthToken() != null;
    }

    /**
     * Check if free mode is enabled (using built-in free models without API key).
     */
    public boolean isFreeModeEnabled() {
        return isFreeModeEnabled;
    }

    /**
     * Enable or disable free mode.
     */
    public void setFreeModeEnabled(boolean enabled) {
        this.isFreeModeEnabled = enabled;
    }

    /**
     * Get the list of built-in free models.
     */
    public List<FreeModel> getBuiltinFreeModels() {
        return BUILTIN_FREE_MODELS;
    }

    /**
     * Get free models as ModelInfo objects for compatibility.
     */
    public List<ModelResponse.ModelInfo> getFreeModelsAsModelInfo() {
        List<ModelResponse.ModelInfo> models = new ArrayList<>();
        for (FreeModel freeModel : BUILTIN_FREE_MODELS) {
            models.add(freeModel.toModelInfo());
        }
        return models;
    }

    /**
     * Get the default free model ID.
     */
    public String getDefaultFreeModelId() {
        return "opencode/sonic";
    }

    /**
     * Get the current authentication token.
     */
    private String getAuthToken() {
        return authTokenProvider != null ? authTokenProvider.get() : null;
    }

    /**
     * Fetch models - either from API (if authenticated) or from built-in free models.
     * Endpoint: GET https://opencode.ai/zen/v1/models
     */
    public void fetchModels() {
        isLoading.setValue(true);
        errorLiveData.setValue(null);

        String authToken = getAuthToken();

        // If free mode is enabled or no auth token, use built-in free models
        if (isFreeModeEnabled || authToken == null) {
            // Check if we should show free models by default when not authenticated
            boolean useFreeModels = isFreeModeEnabled || shouldUseFreeModelsByDefault();

            if (useFreeModels) {
                isLoading.postValue(false);
                List<ModelResponse.ModelInfo> freeModels = getFreeModelsAsModelInfo();
                modelsLiveData.postValue(freeModels);
                Log.d(TAG, "Using built-in free models: " + freeModels.size() + " models available");
                return;
            }

            // Not using free models and no auth token - show error
            if (authToken == null) {
                isLoading.postValue(false);
                errorLiveData.postValue("Not authenticated. Add your OpenCode Zen API key or enable Free Mode.");
                return;
            }
        }

        // Authenticated - fetch from Zen API
        apiService.getModels(authToken).enqueue(new retrofit2.Callback<ModelResponse>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<ModelResponse> call,
                                   @androidx.annotation.NonNull retrofit2.Response<ModelResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getModels() != null) {
                    List<ModelResponse.ModelInfo> models = response.body().getModels();
                    modelsLiveData.postValue(models);
                    cacheModels(models);
                    Log.d(TAG, "Fetched " + models.size() + " models from OpenCode Zen API");
                } else if (response.code() == 401) {
                    errorLiveData.postValue("Invalid API key. Please check your OpenCode Zen credentials.");
                    // Fall back to free models
                    fallbackToFreeModels();
                } else {
                    errorLiveData.postValue("No models available from OpenCode Zen");
                    // Fall back to free models
                    fallbackToFreeModels();
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<ModelResponse> call,
                                  @androidx.annotation.NonNull Throwable t) {
                isLoading.postValue(false);
                String errorMsg = t.getMessage() != null ? t.getMessage() : "Failed to fetch models from OpenCode Zen";
                errorLiveData.postValue(errorMsg);
                Log.e(TAG, "Error fetching models from OpenCode Zen API", t);
                // Fall back to free models on network error
                fallbackToFreeModels();
            }
        });
    }

    /**
     * Check if we should use free models by default when not authenticated.
     */
    private boolean shouldUseFreeModelsByDefault() {
        // Could be configured via settings in the future
        return true; // Enable free models by default for better user experience
    }

    /**
     * Fall back to free models when API fails.
     */
    private void fallbackToFreeModels() {
        List<ModelResponse.ModelInfo> freeModels = getFreeModelsAsModelInfo();
        if (!freeModels.isEmpty()) {
            modelsLiveData.postValue(freeModels);
            Log.d(TAG, "Fell back to " + freeModels.size() + " built-in free models");
        }
    }

    /**
     * Refresh models from API.
     */
    public void refreshModels() {
        fetchModels();
    }

    /**
     * Fetch all models including both API and free models.
     */
    public void fetchAllModels() {
        isLoading.setValue(true);
        errorLiveData.setValue(null);

        String authToken = getAuthToken();
        List<ModelResponse.ModelInfo> allModels = new ArrayList<>();

        // Always include free models
        allModels.addAll(getFreeModelsAsModelInfo());

        // If authenticated, also fetch from API
        if (authToken != null) {
            apiService.getModels(authToken).enqueue(new retrofit2.Callback<ModelResponse>() {
                @Override
                public void onResponse(@androidx.annotation.NonNull retrofit2.Call<ModelResponse> call,
                                       @androidx.annotation.NonNull retrofit2.Response<ModelResponse> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful() && response.body() != null && response.body().getModels() != null) {
                        List<ModelResponse.ModelInfo> apiModels = response.body().getModels();
                        // Merge API models (avoiding duplicates)
                        for (ModelResponse.ModelInfo apiModel : apiModels) {
                            boolean exists = false;
                            for (ModelResponse.ModelInfo freeModel : allModels) {
                                if (apiModel.getId().equals(freeModel.getId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                allModels.add(apiModel);
                            }
                        }
                        modelsLiveData.postValue(allModels);
                        Log.d(TAG, "Combined " + allModels.size() + " models (free + API)");
                    } else {
                        // Just use free models
                        modelsLiveData.postValue(allModels);
                    }
                }

                @Override
                public void onFailure(@androidx.annotation.NonNull retrofit2.Call<ModelResponse> call,
                                      @androidx.annotation.NonNull Throwable t) {
                    isLoading.postValue(false);
                    // Just use free models on failure
                    modelsLiveData.postValue(allModels);
                    Log.d(TAG, "API fetch failed, using " + allModels.size() + " free models");
                }
            });
        } else {
            // Not authenticated, just use free models
            isLoading.postValue(false);
            modelsLiveData.postValue(allModels);
            Log.d(TAG, "Not authenticated, using " + allModels.size() + " free models");
        }
    }

    /**
     * Get cached models.
     */
    private void loadCachedModels() {
        executorService.execute(() -> {
            String cached = settingsDao.getValueByKeySync(CACHE_KEY);
            if (cached != null && !cached.isEmpty()) {
                try {
                    // Parse cached JSON (simplified - in production use Gson)
                    List<ModelResponse.ModelInfo> models = parseCachedModels(cached);
                    if (models != null && !models.isEmpty()) {
                        modelsLiveData.postValue(models);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading cached models", e);
                }
            }
        });
    }

    /**
     * Cache models to local storage.
     */
    private void cacheModels(List<ModelResponse.ModelInfo> models) {
        executorService.execute(() -> {
            // In production, serialize to JSON
            SettingsEntity cacheSetting = new SettingsEntity(CACHE_KEY, String.valueOf(System.currentTimeMillis()));
            settingsDao.insert(cacheSetting);
        });
    }

    /**
     * Get default model - uses free model if in free mode.
     */
    public String getDefaultModel() {
        String savedModel = settingsDao.getDefaultModelSync();
        if (savedModel != null && !savedModel.isEmpty()) {
            return savedModel;
        }
        // Return default free model if in free mode
        if (isFreeModeEnabled) {
            return getDefaultFreeModelId();
        }
        return null;
    }

    /**
     * Set default model.
     */
    public void setDefaultModel(String modelId) {
        executorService.execute(() -> {
            SettingsEntity setting = new SettingsEntity("default_model", modelId);
            settingsDao.insert(setting);
        });
    }

    /**
     * Get model by ID.
     */
    public ModelResponse.ModelInfo getModelById(String modelId) {
        List<ModelResponse.ModelInfo> models = modelsLiveData.getValue();
        if (models != null) {
            return models.stream()
                    .filter(m -> modelId.equals(m.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Get models filtered by provider.
     */
    public List<ModelResponse.ModelInfo> getModelsByProvider(String provider) {
        List<ModelResponse.ModelInfo> models = modelsLiveData.getValue();
        if (models != null) {
            List<ModelResponse.ModelInfo> filtered = new ArrayList<>();
            for (ModelResponse.ModelInfo model : models) {
                if (provider.equals(model.getProviderName())) {
                    filtered.add(model);
                }
            }
            return filtered;
        }
        return new ArrayList<>();
    }

    /**
     * Get only free models from current model list.
     */
    public List<ModelResponse.ModelInfo> getFreeModels() {
        List<ModelResponse.ModelInfo> models = modelsLiveData.getValue();
        if (models != null) {
            List<ModelResponse.ModelInfo> freeModels = new ArrayList<>();
            for (ModelResponse.ModelInfo model : models) {
                if (isFreeModel(model)) {
                    freeModels.add(model);
                }
            }
            return freeModels;
        }
        return new ArrayList<>();
    }

    /**
     * Check if a model is a free model.
     */
    public boolean isFreeModel(ModelResponse.ModelInfo model) {
        if (model == null || model.getPricing() == null) {
            return false;
        }
        // Check if pricing is $0.00
        ModelResponse.ModelInfo.Pricing pricing = model.getPricing();
        String promptPrice = pricing.getPromptTokens();
        String completionPrice = pricing.getCompletionTokens();

        boolean isFree = ("$0.00".equals(promptPrice) || promptPrice == null || promptPrice.isEmpty())
                && ("$0.00".equals(completionPrice) || completionPrice == null || completionPrice.isEmpty());

        // Also check if it's a built-in free model
        if (!isFree) {
            for (FreeModel freeModel : BUILTIN_FREE_MODELS) {
                if (freeModel.getId().equals(model.getId()) ||
                    ("opencode/" + freeModel.getId()).equals(model.getId())) {
                    return true;
                }
            }
        }

        return isFree;
    }

    /**
     * Search models by name.
     */
    public List<ModelResponse.ModelInfo> searchModels(String query) {
        List<ModelResponse.ModelInfo> models = modelsLiveData.getValue();
        if (models != null && query != null) {
            String lowerQuery = query.toLowerCase();
            List<ModelResponse.ModelInfo> results = new ArrayList<>();
            for (ModelResponse.ModelInfo model : models) {
                String displayName = model.getDisplayName().toLowerCase();
                String id = model.getId().toLowerCase();
                if (displayName.contains(lowerQuery) || id.contains(lowerQuery)) {
                    results.add(model);
                }
            }
            return results;
        }
        return new ArrayList<>();
    }

    /**
     * Parse cached model data (simplified).
     */
    private List<ModelResponse.ModelInfo> parseCachedModels(String data) {
        // In production, use Gson to deserialize from JSON
        return new ArrayList<>();
    }

    /**
     * Clean up resources.
     */
    public void cleanup() {
        // No disposable resources in this repository
    }
}
