package com.example.carwatch.ui.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.carwatch.ui.history.HistoryViewModel;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<String> _username = new MutableLiveData<>();
    public LiveData<String> username = _username;

    private final MutableLiveData<String> _imagePath = new MutableLiveData<>();
    public LiveData<String> imagePath = _imagePath;

    private final SharedPreferences sharedPreferences;

    private boolean isImageFetched = false;
    private boolean isHistoryFetched = false;

    private List<HistoryViewModel.UiHistoryItem> lastHistoryItems;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences("my_app", Context.MODE_PRIVATE);
        loadUsername();
    }

    public void loadUsername() {
        String currentUsername = sharedPreferences.getString("username", "User");
        _username.setValue(currentUsername);
    }

    public void setImagePath(String path) {
        _imagePath.setValue(path);
    }

    public boolean isImageFetched() {
        return isImageFetched;
    }

    public void setImageFetched(boolean imageFetched) {
        isImageFetched = imageFetched;
    }

    public boolean isHistoryFetched() {
        return isHistoryFetched;
    }

    public void setHistoryFetched(boolean historyFetched) {
        isHistoryFetched = historyFetched;
    }

    public List<HistoryViewModel.UiHistoryItem> getLastHistoryItems() {
        return lastHistoryItems;
    }

    public void setLastHistoryItems(List<HistoryViewModel.UiHistoryItem> lastHistoryItems) {
        this.lastHistoryItems = lastHistoryItems;
    }
}
