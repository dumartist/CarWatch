package com.example.carwatch.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> _lastDetectionTime = new MutableLiveData<>();
    public LiveData<String> lastDetectionTime = _lastDetectionTime;

    private final MutableLiveData<String> _licensePlate = new MutableLiveData<>();
    public LiveData<String> licensePlate = _licensePlate;

    private final MutableLiveData<Boolean> _isGarageLampOn = new MutableLiveData<>(false);
    public LiveData<Boolean> isGarageLampOn = _isGarageLampOn;

    public void updateDetection(String time, String plate) {
        _lastDetectionTime.setValue(time);
        _licensePlate.setValue(plate);
    }

    public void toggleGarageLamp() {
        Boolean currentState = _isGarageLampOn.getValue();
        _isGarageLampOn.setValue(currentState == null ? true : !currentState);
    }
}
