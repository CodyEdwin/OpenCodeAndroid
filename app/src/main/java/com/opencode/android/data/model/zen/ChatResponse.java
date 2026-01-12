package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for chat completion API.
 */
public class ChatResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("object")
    private String object;

    @SerializedName("created")
    private Long created;

    @SerializedName("model")
    private String model;

    @SerializedName("choices")
    private List<Choice> choices;

    @SerializedName("usage")
    private Usage usage;

    @SerializedName("system_fingerprint")
    private String systemFingerprint;

    public ChatResponse() {
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

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public String getSystemFingerprint() {
        return systemFingerprint;
    }

    public void setSystemFingerprint(String systemFingerprint) {
        this.systemFingerprint = systemFingerprint;
    }

    public Choice getFirstChoice() {
        return choices != null && !choices.isEmpty() ? choices.get(0) : null;
    }

    public String getFirstMessageContent() {
        Choice choice = getFirstChoice();
        return choice != null ? choice.getMessageContent() : null;
    }

    /**
     * Represents a single choice in the response.
     */
    public static class Choice {

        @SerializedName("index")
        private Integer index;

        @SerializedName("message")
        private ChatMessage message;

        @SerializedName("finish_reason")
        private String finishReason;

        @SerializedName("logprobs")
        private LogProbs logProbs;

        public Choice() {
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public ChatMessage getMessage() {
            return message;
        }

        public void setMessage(ChatMessage message) {
            this.message = message;
        }

        public String getMessageContent() {
            return message != null ? message.getContent() : null;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public LogProbs getLogProbs() {
            return logProbs;
        }

        public void setLogProbs(LogProbs logProbs) {
            this.logProbs = logProbs;
        }

        public boolean isComplete() {
            return "stop".equals(finishReason) || "length".equals(finishReason);
        }
    }

    /**
     * Usage statistics for the request.
     */
    public static class Usage {

        @SerializedName("prompt_tokens")
        private Integer promptTokens;

        @SerializedName("completion_tokens")
        private Integer completionTokens;

        @SerializedName("total_tokens")
        private Integer totalTokens;

        @SerializedName("prompt_tokens_details")
        private TokenDetails promptTokensDetails;

        @SerializedName("completion_tokens_details")
        private TokenDetails completionTokensDetails;

        public Usage() {
        }

        public Integer getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        public Integer getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        public Integer getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }

        public TokenDetails getPromptTokensDetails() {
            return promptTokensDetails;
        }

        public void setPromptTokensDetails(TokenDetails promptTokensDetails) {
            this.promptTokensDetails = promptTokensDetails;
        }

        public TokenDetails getCompletionTokensDetails() {
            return completionTokensDetails;
        }

        public void setCompletionTokensDetails(TokenDetails completionTokensDetails) {
            this.completionTokensDetails = completionTokensDetails;
        }
    }

    /**
     * Token usage details.
     */
    public static class TokenDetails {

        @SerializedName("cached_tokens")
        private Integer cachedTokens;

        @SerializedName("audio_tokens")
        private Integer audioTokens;

        @SerializedName("reasoning_tokens")
        private Integer reasoningTokens;

        public TokenDetails() {
        }

        public Integer getCachedTokens() {
            return cachedTokens;
        }

        public void setCachedTokens(Integer cachedTokens) {
            this.cachedTokens = cachedTokens;
        }

        public Integer getAudioTokens() {
            return audioTokens;
        }

        public void setAudioTokens(Integer audioTokens) {
            this.audioTokens = audioTokens;
        }

        public Integer getReasoningTokens() {
            return reasoningTokens;
        }

        public void setReasoningTokens(Integer reasoningTokens) {
            this.reasoningTokens = reasoningTokens;
        }
    }

    /**
     * Log probability information.
     */
    public static class LogProbs {

        @SerializedName("content")
        private List<ContentLogProb> content;

        public LogProbs() {
        }

        public List<ContentLogProb> getContent() {
            return content;
        }

        public void setContent(List<ContentLogProb> content) {
            this.content = content;
        }
    }

    /**
     * Content log probability.
     */
    public static class ContentLogProb {

        @SerializedName("token")
        private String token;

        @SerializedName("logprob")
        private Double logprob;

        @SerializedName("bytes")
        private List<Integer> bytes;

        public ContentLogProb() {
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Double getLogprob() {
            return logprob;
        }

        public void setLogprob(Double logprob) {
            this.logprob = logprob;
        }

        public List<Integer> getBytes() {
            return bytes;
        }

        public void setBytes(List<Integer> bytes) {
            this.bytes = bytes;
        }
    }
}
