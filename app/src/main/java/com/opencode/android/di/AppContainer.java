package com.opencode.android.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.opencode.android.data.local.OpenCodeDatabase;
import com.opencode.android.data.local.dao.MessageDao;
import com.opencode.android.data.local.dao.SessionDao;
import com.opencode.android.data.local.dao.SettingsDao;
import com.opencode.android.data.remote.zen.ZenApiService;
import com.opencode.android.data.repository.ChatRepository;
import com.opencode.android.data.repository.ChatRepositoryImpl;
import com.opencode.android.data.repository.ModelRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Manual Dependency Injection Container for OpenCode Android.
 * Provides singleton instances of all dependencies without using Hilt/Dagger.
 */
public class AppContainer {

    private static final String TAG = "AppContainer";
    private static final String PREF_NAME = "secure_prefs";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_API_BASE_URL = "api_base_url";

    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 60;
    private static final int WRITE_TIMEOUT = 30;

    private static volatile AppContainer INSTANCE;
    private final Context applicationContext;

    // Network components
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private ZenApiService zenApiService;

    // Database components
    private OpenCodeDatabase database;
    private SessionDao sessionDao;
    private MessageDao messageDao;
    private SettingsDao settingsDao;

    // Executors
    private ExecutorService executorService;

    // Repositories
    private ChatRepository chatRepository;
    private ModelRepository modelRepository;

    private AppContainer(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    /**
     * Get the singleton instance of AppContainer.
     */
    public static AppContainer getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppContainer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppContainer(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Initialize all dependencies. Should be called once during application startup.
     */
    public void initialize() {
        initDatabase();
        initNetwork();
        initRepositories();
    }

    private void initDatabase() {
        database = OpenCodeDatabase.getInstance(applicationContext);
        sessionDao = database.sessionDao();
        messageDao = database.messageDao();
        settingsDao = database.settingsDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    private void initNetwork() {
        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Build OkHttpClient (no auth interceptor - we pass token directly in API calls)
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .retryOnConnectionFailure(true)
                .build();

        // Get base URL from preferences or use default OpenCode Zen endpoint
        String baseUrl = getBaseUrl();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        zenApiService = retrofit.create(ZenApiService.class);
    }

    /**
     * Get authentication token for OpenCode Zen API.
     * Uses encrypted shared preferences for secure storage.
     */
    public String getAuthToken() {
        try {
            SharedPreferences prefs = getSecurePreferences();
            String apiKey = prefs.getString(KEY_API_KEY, null);
            if (apiKey != null && !apiKey.isEmpty()) {
                return "Bearer " + apiKey;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting auth token", e);
        }
        return null;
    }

    /**
     * Check if user is authenticated with OpenCode Zen.
     */
    public boolean isAuthenticated() {
        return getAuthToken() != null;
    }

    /**
     * Save API key securely.
     */
    public boolean saveApiKey(String apiKey) {
        try {
            SharedPreferences prefs = getSecurePreferences();
            prefs.edit().putString(KEY_API_KEY, apiKey).apply();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving API key", e);
            return false;
        }
    }

    /**
     * Clear API key.
     */
    public void clearApiKey() {
        try {
            SharedPreferences prefs = getSecurePreferences();
            prefs.edit().remove(KEY_API_KEY).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing API key", e);
        }
    }

    private void initRepositories() {
        // Create auth token supplier
        Supplier<String> authTokenSupplier = this::getAuthToken;

        chatRepository = new ChatRepositoryImpl(
                sessionDao,
                messageDao,
                zenApiService,
                executorService,
                authTokenSupplier
        );

        modelRepository = new ModelRepository(
                zenApiService,
                settingsDao,
                executorService,
                authTokenSupplier
        );
    }

    private String getBaseUrl() {
        SharedPreferences prefs = applicationContext.getSharedPreferences("opencode_prefs", Context.MODE_PRIVATE);
        return prefs.getString(KEY_API_BASE_URL, "https://opencode.ai/zen/v1/");
    }

    // Getters for all dependencies

    public ZenApiService getZenApiService() {
        return zenApiService;
    }

    public ChatRepository getChatRepository() {
        return chatRepository;
    }

    public ModelRepository getModelRepository() {
        return modelRepository;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    public MessageDao getMessageDao() {
        return messageDao;
    }

    public SettingsDao getSettingsDao() {
        return settingsDao;
    }

    /**
     * Get encrypted shared preferences for secure storage.
     */
    private SharedPreferences getSecurePreferences() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                applicationContext,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /**
     * Clean up resources. Should be called when application is terminated.
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (chatRepository instanceof ChatRepositoryImpl) {
            ((ChatRepositoryImpl) chatRepository).cleanup();
        }
        if (modelRepository != null) {
            modelRepository.cleanup();
        }
    }
}
