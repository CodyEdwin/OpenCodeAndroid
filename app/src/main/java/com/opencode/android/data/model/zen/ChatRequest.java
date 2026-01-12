package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request model for chat completion API.
 */
public class ChatRequest {

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<ChatMessage> messages;

    @SerializedName("max_tokens")
    private Integer maxTokens;

    @SerializedName("temperature")
    private Double temperature;

    @SerializedName("top_p")
    private Double topP;

    @SerializedName("stream")
    private Boolean stream;

    @SerializedName("tools")
    private List<ToolDefinition> tools;

    public ChatRequest() {
        this.stream = true;
    }

    public ChatRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        this.messages = messages;
        this.stream = true;
    }

    // Getters and Setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public List<ToolDefinition> getTools() {
        return tools;
    }

    public void setTools(List<ToolDefinition> tools) {
        this.tools = tools;
    }

    /**
     * Builder pattern for easier construction.
     */
    public static class Builder {
        private String model;
        private List<ChatMessage> messages;
        private Integer maxTokens;
        private Double temperature;
        private Double topP;
        private Boolean stream = true;
        private List<ToolDefinition> tools;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder messages(List<ChatMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder tools(List<ToolDefinition> tools) {
            this.tools = tools;
            return this;
        }

        public ChatRequest build() {
            ChatRequest request = new ChatRequest();
            request.model = this.model;
            request.messages = this.messages;
            request.maxTokens = this.maxTokens;
            request.temperature = this.temperature;
            request.topP = this.topP;
            request.stream = this.stream;
            request.tools = this.tools;
            return request;
        }
    }
}
