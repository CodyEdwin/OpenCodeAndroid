package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Represents a tool call from the AI model.
 */
public class ToolCall {

    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("function")
    private FunctionInfo function;

    public ToolCall() {
    }

    public ToolCall(String id, String functionName, String functionArguments) {
        this.id = id;
        this.type = "function";
        this.function = new FunctionInfo(functionName, functionArguments);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FunctionInfo getFunction() {
        return function;
    }

    public void setFunction(FunctionInfo function) {
        this.function = function;
    }

    public String getFunctionName() {
        return function != null ? function.getName() : null;
    }

    public String getFunctionArguments() {
        return function != null ? function.getArguments() : null;
    }

    public boolean isValid() {
        return id != null && !id.isEmpty() && function != null;
    }

    /**
     * Function information within a tool call.
     */
    public static class FunctionInfo {

        @SerializedName("name")
        private String name;

        @SerializedName("arguments")
        private String arguments;

        public FunctionInfo() {
        }

        public FunctionInfo(String name, String arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArguments() {
            return arguments;
        }

        public void setArguments(String arguments) {
            this.arguments = arguments;
        }
    }
}
