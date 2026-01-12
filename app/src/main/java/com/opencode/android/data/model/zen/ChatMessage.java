package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single message in a chat conversation.
 */
public class ChatMessage {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_TOOL = "tool";

    @SerializedName("role")
    private String role;

    @SerializedName("content")
    private String content;

    @SerializedName("name")
    private String name;

    @SerializedName("tool_calls")
    private ToolCall[] toolCalls;

    @SerializedName("tool_call_id")
    private String toolCallId;

    @SerializedName("reasoning_content")
    private String reasoningContent;

    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Static factory methods
    public static ChatMessage user(String content) {
        return new ChatMessage(ROLE_USER, content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage(ROLE_ASSISTANT, content);
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(ROLE_SYSTEM, content);
    }

    public static ChatMessage tool(String content, String toolCallId) {
        ChatMessage message = new ChatMessage(ROLE_TOOL, content);
        message.setToolCallId(toolCallId);
        return message;
    }

    // Getters and Setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
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

    public ToolCall[] getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(ToolCall[] toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public void setReasoningContent(String reasoningContent) {
        this.reasoningContent = reasoningContent;
    }

    public boolean isUser() {
        return ROLE_USER.equals(role);
    }

    public boolean isAssistant() {
        return ROLE_ASSISTANT.equals(role);
    }

    public boolean isSystem() {
        return ROLE_SYSTEM.equals(role);
    }

    public boolean isTool() {
        return ROLE_TOOL.equals(role);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && toolCalls.length > 0;
    }

    /**
     * Creates a copy of this message with the specified content.
     */
    public ChatMessage copyWithContent(String newContent) {
        ChatMessage copy = new ChatMessage(this.role, newContent);
        copy.name = this.name;
        copy.toolCalls = this.toolCalls;
        copy.toolCallId = this.toolCallId;
        copy.reasoningContent = this.reasoningContent;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatMessage that = (ChatMessage) o;

        if (role != null ? !role.equals(that.role) : that.role != null) return false;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "role='" + role + '\'' +
                ", content='" + (content != null && content.length() > 50
                    ? content.substring(0, 50) + "..." : content) + '\'' +
                '}';
    }
}
