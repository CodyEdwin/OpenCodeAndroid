package com.opencode.android.data.model.zen;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a parameter specification for a tool.
 */
public class ToolParameter {

    @SerializedName("type")
    private String type;

    @SerializedName("description")
    private String description;

    @SerializedName("enum_values")
    private String[] enumValues;

    @SerializedName("default")
    private String defaultValue;

    @SerializedName("minimum")
    private Integer minimum;

    @SerializedName("maximum")
    private Integer maximum;

    public ToolParameter() {
    }

    public ToolParameter(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public ToolParameter(String type, String description, boolean required) {
        this.type = type;
        this.description = description;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(String[] enumValues) {
        this.enumValues = enumValues;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getMinimum() {
        return minimum;
    }

    public void setMinimum(Integer minimum) {
        this.minimum = minimum;
    }

    public Integer getMaximum() {
        return maximum;
    }

    public void setMaximum(Integer maximum) {
        this.maximum = maximum;
    }

    public boolean isString() {
        return "string".equals(type);
    }

    public boolean isNumber() {
        return "number".equals(type) || "integer".equals(type);
    }

    public boolean isBoolean() {
        return "boolean".equals(type);
    }

    public boolean hasEnumValues() {
        return enumValues != null && enumValues.length > 0;
    }

    /**
     * Common parameter types.
     */
    public static class Types {
        public static final String STRING = "string";
        public static final String NUMBER = "number";
        public static final String INTEGER = "integer";
        public static final String BOOLEAN = "boolean";
        public static final String ARRAY = "array";
        public static final String OBJECT = "object";
    }
}
