package com.example.carwatch.ui.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** @noinspection CanBeFinal*/
public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<String> _username = new MutableLiveData<>();
    public LiveData<String> username = _username;

    private final MutableLiveData<String> _imagePath = new MutableLiveData<>();
    public LiveData<String> imagePath = _imagePath;

    private final SharedPreferences sharedPreferences;

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
}