package com.opencode.android.ui.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for model selection screen.
 */
public class ModelSelectionViewModel extends ViewModel {

    private final MutableLiveData<List<ModelItem>> models = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<ModelItem>> getModels() {
        return models;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadModels() {
        isLoading.setValue(true);
        // Load models from repository
        // This would be implemented with actual data loading
    }

    public void selectModel(ModelItem model) {
        // Handle model selection
    }
}
