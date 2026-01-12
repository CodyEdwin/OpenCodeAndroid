package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

/**
 * Streaming response chunk for SSE (Server-Sent Events) from chat completion API.
 */
public class StreamingResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("object")
    private String object;

    @SerializedName("created")
    private Long created;

    @SerializedName("model")
    private String model;

    @SerializedName("choices")
    private Choice[] choices;

    @SerializedName("usage")
    private ChatResponse.Usage usage;

    @SerializedName("system_fingerprint")
    private String systemFingerprint;

    public StreamingResponse() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Choice[] getChoices() {
        return choices;
    }

    public void setChoices(Choice[] choices) {
        this.choices = choices;
    }

    public ChatResponse.Usage getUsage() {
        return usage;
    }

    public void setUsage(ChatResponse.Usage usage) {
        this.usage = usage;
    }

    public String getSystemFingerprint() {
        return systemFingerprint;
    }

    public void setSystemFingerprint(String systemFingerprint) {
        this.systemFingerprint = systemFingerprint;
    }

    public Choice getFirstChoice() {
        return choices != null && choices.length > 0 ? choices[0] : null;
    }

    public String getDeltaContent() {
        Choice choice = getFirstChoice();
        if (choice != null && choice.getDelta() != null) {
            return choice.getDelta().getContent();
        }
        return null;
    }

    public boolean isDone() {
        Choice choice = getFirstChoice();
        return choice != null && choice.isComplete();
    }

    /**
     * Represents a streaming choice with delta updates.
     */
    public static class Choice {

        @SerializedName("index")
        private Integer index;

        @SerializedName("delta")
        private Delta delta;

        @SerializedName("finish_reason")
        private String finishReason;

        @SerializedName("logprobs")
        private ChatResponse.LogProbs logProbs;

        public Choice() {
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public Delta getDelta() {
            return delta;
        }

        public void setDelta(Delta delta) {
            this.delta = delta;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public ChatResponse.LogProbs getLogProbs() {
            return logProbs;
        }

        public void setLogProbs(ChatResponse.LogProbs logProbs) {
            this.logProbs = logProbs;
        }

        public boolean isComplete() {
            return finishReason != null && !finishReason.isEmpty();
        }
    }

    /**
     * Represents a delta update in streaming response.
     */
    public static class Delta {

        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        @SerializedName("tool_calls")
        private ToolCall[] toolCalls;

        public Delta() {
        }

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

        public ToolCall[] getToolCalls() {
            return toolCalls;
        }

        public void setToolCalls(ToolCall[] toolCalls) {
            this.toolCalls = toolCalls;
        }

        public boolean hasContent() {
            return content != null && !content.isEmpty();
        }

        public boolean hasToolCalls() {
            return toolCalls != null && toolCalls.length > 0;
        }
    }
}
