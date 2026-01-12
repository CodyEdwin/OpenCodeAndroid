package com.opencode.android.ui.models;

/**
 * Data class representing a model item in the selection list.
 */
public class ModelItem {
    private final String id;
    private final String name;
    private final String provider;
    private final String description;
    private final boolean isSelected;

    public ModelItem(String id, String name, String provider, String description, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.provider = provider;
        this.description = description;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
