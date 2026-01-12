package com.opencode.android;

import android.app.Application;

import androidx.work.Configuration;

import com.opencode.android.di.AppContainer;

import androidx.work.WorkManager;

/**
 * Main Application class for OpenCode Android.
 * Initializes manual dependency injection and WorkManager.
 */
public class OpenCodeApplication extends Application {

    private static OpenCodeApplication instance;
    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initApp();
    }

    private void initApp() {
        // Initialize dependency injection container
        appContainer = AppContainer.getInstance(this);
        appContainer.initialize();

        // Initialize WorkManager with default configuration
        WorkManager.initialize(this, new Configuration.Builder().build());
    }

    public static OpenCodeApplication getInstance() {
        return instance;
    }

    public static AppContainer getAppContainer() {
        return instance != null ? instance.appContainer : null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (appContainer != null) {
            appContainer.cleanup();
        }
    }
}
