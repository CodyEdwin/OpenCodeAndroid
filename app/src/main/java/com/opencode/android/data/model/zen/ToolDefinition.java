package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Represents a tool definition that can be used by the AI model.
 */
public class ToolDefinition {

    public static final String TYPE_FUNCTION = "function";

    @SerializedName("type")
    private String type;

    @SerializedName("function")
    private FunctionDefinition function;

    public ToolDefinition() {
        this.type = TYPE_FUNCTION;
    }

    public ToolDefinition(String name, String description, Map<String, ToolParameter> parameters) {
        this.type = TYPE_FUNCTION;
        this.function = new FunctionDefinition(name, description, parameters);
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FunctionDefinition getFunction() {
        return function;
    }

    public void setFunction(FunctionDefinition function) {
        this.function = function;
    }

    public String getName() {
        return function != null ? function.getName() : null;
    }

    public String getDescription() {
        return function != null ? function.getDescription() : null;
    }

    /**
     * Function definition for tool specification.
     */
    public static class FunctionDefinition {

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("parameters")
        private ParametersDefinition parameters;

        public FunctionDefinition() {
        }

        public FunctionDefinition(String name, String description, Map<String, ToolParameter> parameters) {
            this.name = name;
            this.description = description;
            this.parameters = new ParametersDefinition(parameters, null);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ParametersDefinition getParameters() {
            return parameters;
        }

        public void setParameters(ParametersDefinition parameters) {
            this.parameters = parameters;
        }

        /**
         * Parameters definition for function.
         */
        public static class ParametersDefinition {

            @SerializedName("type")
            private String type = "object";

            @SerializedName("properties")
            private Map<String, ToolParameter> properties;

            @SerializedName("required")
            private String[] required;

            public ParametersDefinition() {
                this.type = "object";
            }

            public ParametersDefinition(Map<String, ToolParameter> properties, String[] required) {
                this.type = "object";
                this.properties = properties;
                this.required = required;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Map<String, ToolParameter> getProperties() {
                return properties;
            }

            public void setProperties(Map<String, ToolParameter> properties) {
                this.properties = properties;
            }

            public String[] getRequired() {
                return required;
            }

            public void setRequired(String[] required) {
                this.required = required;
            }
        }
    }

    /**
     * Predefined tool constants for OpenCode functionality.
     */
    public static class Tools {

        public static ToolDefinition FILE_READ = new ToolDefinition(
                "file_read",
                "Read the contents of a file",
                Map.of(
                        "path", new ToolParameter("string", "The path to the file to read", true)
                )
        );

        public static ToolDefinition FILE_WRITE = new ToolDefinition(
                "file_write",
                "Write content to a file, creating if necessary",
                Map.of(
                        "path", new ToolParameter("string", "The path to the file to write", true),
                        "content", new ToolParameter("string", "The content to write to the file", true)
                )
        );

        public static ToolDefinition FILE_EDIT = new ToolDefinition(
                "file_edit",
                "Edit a specific section of a file",
                Map.of(
                        "path", new ToolParameter("string", "The path to the file to edit", true),
                        "old_string", new ToolParameter("string", "The exact text to replace", true),
                        "new_string", new ToolParameter("string", "The text to replace with", true)
                )
        );

        public static ToolDefinition FILE_LIST = new ToolDefinition(
                "file_list",
                "List files in a directory",
                Map.of(
                        "path", new ToolParameter("string", "The directory path to list", false)
                )
        );

        public static ToolDefinition FILE_GLOB = new ToolDefinition(
                "file_glob",
                "Find files matching a glob pattern",
                Map.of(
                        "pattern", new ToolParameter("string", "The glob pattern (e.g., **/*.java)", true),
                        "path", new ToolParameter("string", "The root path to search from", false)
                )
        );

        public static ToolDefinition FILE_GREP = new ToolDefinition(
                "file_grep",
                "Search for text in files using regex",
                Map.of(
                        "pattern", new ToolParameter("string", "The regex pattern to search for", true),
                        "path", new ToolParameter("string", "The directory to search in", false),
                        "extension", new ToolParameter("string", "File extension filter", false)
                )
        );

        public static ToolDefinition COMMAND_EXECUTE = new ToolDefinition(
                "command_execute",
                "Execute a shell command and return the output",
                Map.of(
                        "command", new ToolParameter("string", "The shell command to execute", true),
                        "timeout", new ToolParameter("number", "Timeout in seconds (default: 30)", false)
                )
        );
    }
}
