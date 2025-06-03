package com.example.carwatch.ui.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<String> _lastDetectionTime = new MutableLiveData<>();
    public LiveData<String> lastDetectionTime = _lastDetectionTime;

    private final MutableLiveData<String> _licensePlate = new MutableLiveData<>();
    public LiveData<String> licensePlate = _licensePlate;

    private final MutableLiveData<Boolean> _isGarageLampOn = new MutableLiveData<>(false);
    public LiveData<Boolean> isGarageLampOn = _isGarageLampOn;

    private final MutableLiveData<String> _username = new MutableLiveData<>();
    public LiveData<String> username = _username;

    private SharedPreferences sharedPreferences;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences("my_app", Context.MODE_PRIVATE);
        loadUsername();
    }

    public void loadUsername() {
        String currentUsername = sharedPreferences.getString("username", "User");
        _username.setValue(currentUsername);
    }

    public void updateDetection(String time, String plate) {
        _lastDetectionTime.setValue(time);
        _licensePlate.setValue(plate);
    }

    public void toggleGarageLamp() {
        Boolean currentState = _isGarageLampOn.getValue();
        _isGarageLampOn.setValue(currentState == null ? true : !currentState);
    }
}
