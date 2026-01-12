package com.opencode.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

/**
 * Entity representing a single message in a chat session.
 */
@Entity(
    tableName = "messages",
    foreignKeys = {
        @ForeignKey(
            entity = SessionEntity.class,
            parentColumns = "id",
            childColumns = "session_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "session_id"),
        @Index(value = "created_at"),
        @Index(value = "role")
    }
)
public class MessageEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private String id;

    @ColumnInfo(name = "session_id")
    @NonNull
    private String sessionId;

    @ColumnInfo(name = "role")
    @NonNull
    private String role;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "tool_calls")
    private String toolCalls;

    @ColumnInfo(name = "tool_call_id")
    private String toolCallId;

    @ColumnInfo(name = "token_count")
    private int tokenCount;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "is_streaming")
    private boolean isStreaming;

    @ColumnInfo(name = "is_complete")
    private boolean isComplete;

    @ColumnInfo(name = "metadata")
    private String metadata;

    public MessageEntity() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = new Date();
    }

    public MessageEntity(String sessionId, String role, String content) {
        this();
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
    }

    // Static factory methods
    public static MessageEntity user(String sessionId, String content) {
        return new MessageEntity(sessionId, "user", content);
    }

    public static MessageEntity assistant(String sessionId, String content) {
        return new MessageEntity(sessionId, "assistant", content);
    }

    public static MessageEntity system(String sessionId, String content) {
        return new MessageEntity(sessionId, "system", content);
    }

    public static MessageEntity tool(String sessionId, String content, String toolCallId) {
        MessageEntity message = new MessageEntity(sessionId, "tool", content);
        message.setToolCallId(toolCallId);
        return message;
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(@NonNull String sessionId) {
        this.sessionId = sessionId;
    }

    @NonNull
    public String getRole() {
        return role;
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(String toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public void setStreaming(boolean streaming) {
        isStreaming = streaming;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    // Role check methods
    public boolean isUser() {
        return "user".equals(role);
    }

    public boolean isAssistant() {
        return "assistant".equals(role);
    }

    public boolean isSystem() {
        return "system".equals(role);
    }

    public boolean isTool() {
        return "tool".equals(role);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    /**
     * Update content for streaming responses.
     */
    public void appendContent(String delta) {
        if (content == null) {
            content = delta;
        } else {
            content += delta;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntity that = (MessageEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
                "id='" + id + '\'' +
                ", role='" + role + '\'' +
                ", content='" + (content != null && content.length() > 50
                    ? content.substring(0, 50) + "..." : content) + '\'' +
                '}';
    }
}
