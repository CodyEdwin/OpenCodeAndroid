package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Response model for the models list API.
 */
public class ModelResponse {

    @SerializedName("object")
    private String object;

    @SerializedName("data")
    private List<ModelInfo> data;

    public ModelResponse() {
    }

    // Getters and Setters
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<ModelInfo> getData() {
        return data;
    }

    public void setData(List<ModelInfo> data) {
        this.data = data;
    }

    public List<ModelInfo> getModels() {
        return data;
    }

    /**
     * Represents information about a specific model.
     */
    public static class ModelInfo {

        @SerializedName("id")
        private String id;

        @SerializedName("object")
        private String object;

        @SerializedName("created")
        private Long created;

        @SerializedName("owned_by")
        private String ownedBy;

        @SerializedName("root")
        private String root;

        @SerializedName("parent")
        private String parent;

        @SerializedName("capabilities")
        private Map<String, Boolean> capabilities;

        @SerializedName("pricing")
        private Pricing pricing;

        public ModelInfo() {
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

        public String getOwnedBy() {
            return ownedBy;
        }

        public void setOwnedBy(String ownedBy) {
            this.ownedBy = ownedBy;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public Map<String, Boolean> getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(Map<String, Boolean> capabilities) {
            this.capabilities = capabilities;
        }

        public Pricing getPricing() {
            return pricing;
        }

        public void setPricing(Pricing pricing) {
            this.pricing = pricing;
        }

        // Capability check methods
        public boolean supportsStreaming() {
            return capabilities != null && Boolean.TRUE.equals(capabilities.get("stream"));
        }

        public boolean supportsTools() {
            return capabilities != null && Boolean.TRUE.equals(capabilities.get("tools"));
        }

        public boolean supportsVision() {
            return capabilities != null && Boolean.TRUE.equals(capabilities.get("vision"));
        }

        /**
         * Pricing information for the model.
         */
        public static class Pricing {

            @SerializedName("prompt_tokens")
            private String promptTokens;

            @SerializedName("completion_tokens")
            private String completionTokens;

            @SerializedName("image_tokens")
            private String imageTokens;

            @SerializedName("request")
            private String request;

            public Pricing() {
            }

            public String getPromptTokens() {
                return promptTokens;
            }

            public void setPromptTokens(String promptTokens) {
                this.promptTokens = promptTokens;
            }

            public String getCompletionTokens() {
                return completionTokens;
            }

            public void setCompletionTokens(String completionTokens) {
                this.completionTokens = completionTokens;
            }

            public String getImageTokens() {
                return imageTokens;
            }

            public void setImageTokens(String imageTokens) {
                this.imageTokens = imageTokens;
            }

            public String getRequest() {
                return request;
            }

            public void setRequest(String request) {
                this.request = request;
            }

            public double getPromptPricePerMillion() {
                return parsePrice(promptTokens);
            }

            public double getCompletionPricePerMillion() {
                return parsePrice(completionTokens);
            }

            private double parsePrice(String price) {
                if (price == null || price.isEmpty()) {
                    return 0.0;
                }
                try {
                    // Price format: "$X.XX" or "X.XX"
                    String cleanPrice = price.replace("$", "").trim();
                    return Double.parseDouble(cleanPrice);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }

        /**
         * Extract provider name from model ID.
         */
        public String getProviderName() {
            if (id == null) {
                return "Unknown";
            }
            if (id.startsWith("gpt-")) {
                return "OpenAI";
            } else if (id.startsWith("claude-")) {
                return "Anthropic";
            } else if (id.startsWith("gemini-")) {
                return "Google";
            } else if (id.startsWith("glm-") || id.startsWith("kimi-")) {
                return "Zhipu AI";
            } else if (id.startsWith("qwen-")) {
                return "Alibaba";
            } else if (id.startsWith("grok-")) {
                return "xAI";
            } else if (id.startsWith("opencode/")) {
                return "OpenCode Zen";
            }
            return "Other";
        }

        /**
         * Get a display-friendly name for the model.
         */
        public String getDisplayName() {
            if (id == null) {
                return "Unknown Model";
            }
            // Remove provider prefix and format nicely
            String name = id.replace("opencode/", "");
            // Convert to title case with spaces
            return name.replace("-", " ")
                    .replace("_", " ")
                    .trim();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModelInfo modelInfo = (ModelInfo) o;
            return id != null ? id.equals(modelInfo.id) : modelInfo.id == null;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
}
