package com.opencode.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity for storing key-value settings.
 */
@Entity(
    tableName = "settings",
    indices = {
        @Index(value = "key", unique = true)
    }
)
public class SettingsEntity {

    @PrimaryKey
    @ColumnInfo(name = "key")
    @NonNull
    private String key;

    @ColumnInfo(name = "value")
    private String value;

    @ColumnInfo(name = "is_encrypted")
    private boolean isEncrypted;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public SettingsEntity() {
        this.key = "";
        this.updatedAt = System.currentTimeMillis();
    }

    public SettingsEntity(String key, String value) {
        this();
        this.key = key;
        this.value = value;
    }

    public SettingsEntity(String key, String value, boolean isEncrypted) {
        this(key, value);
        this.isEncrypted = isEncrypted;
    }

    // Getters and Setters
    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Common setting keys
    public static class Keys {
        // API Configuration
        public static final String API_KEY = "api_key";
        public static final String API_BASE_URL = "api_base_url";
        public static final String DEFAULT_MODEL = "default_model";
        public static final String PROVIDER = "provider";

        // Appearance
        public static final String THEME = "theme";
        public static final String FONT_SIZE = "font_size";
        public static final String CODE_FONT = "code_font";

        // Behavior
        public static final String AUTO_SAVE = "auto_save";
        public static final String STREAMING_ENABLED = "streaming_enabled";
        public static final String SYNTAX_HIGHLIGHT = "syntax_highlight";

        // User Data
        public static final String USER_NAME = "user_name";
        public static final String USER_EMAIL = "user_email";

        // App State
        public static final String LAST_SESSION_ID = "last_session_id";
        public static final String FIRST_LAUNCH = "first_launch";
        public static final String ONBOARDING_COMPLETED = "onboarding_completed";
    }

    // Default values
    public static class Defaults {
        public static final String API_BASE_URL = "https://opencode.ai/zen/v1";
        public static final String THEME = "system";
        public static final String FONT_SIZE = "medium";
        public static final String CODE_FONT = "monospace";
        public static final boolean AUTO_SAVE = true;
        public static final boolean STREAMING_ENABLED = true;
        public static final boolean SYNTAX_HIGHLIGHT = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsEntity that = (SettingsEntity) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
