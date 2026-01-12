package com.opencode.android.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.opencode.android.data.local.dao.MessageDao;
import com.opencode.android.data.local.dao.ProjectDao;
import com.opencode.android.data.local.dao.SessionDao;
import com.opencode.android.data.local.dao.SettingsDao;
import com.opencode.android.data.local.entity.MessageEntity;
import com.opencode.android.data.local.entity.ProjectEntity;
import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.data.local.entity.SettingsEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database for OpenCode Android.
 * Manages sessions, messages, settings, and projects.
 */
@Database(
    entities = {
        SessionEntity.class,
        MessageEntity.class,
        SettingsEntity.class,
        ProjectEntity.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter.class)
public abstract class OpenCodeDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "opencode_db";
    private static volatile OpenCodeDatabase INSTANCE;

    // Thread pool for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // DAOs
    public abstract SessionDao sessionDao();
    public abstract MessageDao messageDao();
    public abstract SettingsDao settingsDao();
    public abstract ProjectDao projectDao();

    /**
     * Get the singleton instance of the database.
     */
    public static OpenCodeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (OpenCodeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private static OpenCodeDatabase buildDatabase(Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                OpenCodeDatabase.class,
                DATABASE_NAME
            )
            .addCallback(new DatabaseCallback())
            .fallbackToDestructiveMigration()
            .build();
    }

    /**
     * Database callback for initialization tasks.
     */
    private static class DatabaseCallback extends Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Initialize default settings on database creation
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null) {
                    initializeDefaultSettings();
                }
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Perform any open tasks here
        }
    }

    /**
     * Initialize default settings.
     */
    private static void initializeDefaultSettings() {
        SettingsDao settingsDao = INSTANCE.settingsDao();

        // API Configuration
        settingsDao.insert(new SettingsEntity("api_base_url", "https://opencode.ai/zen/v1"));

        // Appearance
        settingsDao.insert(new SettingsEntity("theme", "system"));
        settingsDao.insert(new SettingsEntity("font_size", "medium"));
        settingsDao.insert(new SettingsEntity("code_font", "monospace"));

        // Behavior
        settingsDao.insert(new SettingsEntity("auto_save", "true"));
        settingsDao.insert(new SettingsEntity("streaming_enabled", "true"));
        settingsDao.insert(new SettingsEntity("syntax_highlight", "true"));

        // App State
        settingsDao.insert(new SettingsEntity("first_launch", "true"));
        settingsDao.insert(new SettingsEntity("onboarding_completed", "false"));
    }

    /**
     * Close the database instance.
     */
    public static void closeDatabase() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }

    /**
     * Clear all tables (保留 schema，删除所有数据).
     */
    public void clearAllTables() {
        databaseWriteExecutor.execute(() -> {
            sessionDao().deleteAll();
            messageDao().deleteAll();
            settingsDao().deleteAll();
            projectDao().deleteAll();
        });
    }

    /**
     * Run a read operation on the database.
     */
    public void runRead(Runnable runnable) {
        databaseWriteExecutor.execute(runnable);
    }
}
