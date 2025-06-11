package com.example.carwatch.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.carwatch.model.LoginData;
import com.example.carwatch.model.LoginResponse;
import com.example.carwatch.model.RegisterResponse;
import com.example.carwatch.network.ApiService;
import com.example.carwatch.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final ApiService apiService;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();
    private final MutableLiveData<String> usernameLiveData = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService(application.getApplicationContext());
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }

    public LiveData<String> getUsernameLiveData() {
        return usernameLiveData;
    }

    public void login(String username, String password) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);

        apiService.login(loginData).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess() && loginResponse.getData() != null) {
                        loginResult.setValue(new LoginResult(loginResponse.getData(), null));
                        usernameLiveData.setValue(loginResponse.getData().getUsername());
                    } else {
                        loginResult.setValue(new LoginResult(null, loginResponse.getMessage()));
                    }
                } else {
                    String errorMsg = "Login failed: " + response.code();
                    try {
                        if(response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("LoginViewModel", "Error parsing error body", e);
                    }
                    loginResult.setValue(new LoginResult(null, errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                loginResult.setValue(new LoginResult(null, "Login failed: " + t.getMessage()));
            }
        });
    }

    public void register(String username, String password) {
        Map<String, String> registerData = new HashMap<>();
        registerData.put("username", username);
        registerData.put("password", password);

        apiService.register(registerData).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse.isSuccess()) {
                        registerResult.setValue(new RegisterResult(true, registerResponse.getMessage()));
                    } else {
                        registerResult.setValue(new RegisterResult(false, registerResponse.getMessage()));
                    }
                } else {
                     String errorMsg = "Registration failed: " + response.code();
                    try {
                        if(response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("LoginViewModel", "Error parsing error body", e);
                    }
                    registerResult.setValue(new RegisterResult(false, errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                registerResult.setValue(new RegisterResult(false, "Registration failed: " + t.getMessage()));
            }
        });
    }

    public static class LoginResult {
        private final LoginData successData;
        private final String error;

        LoginResult(LoginData successData, String error) {
            this.successData = successData;
            this.error = error;
        }

        public LoginData getSuccessData() {
            return successData;
        }

        public String getError() {
            return error;
        }
    }

    public static class RegisterResult {
        private final boolean success;
        private final String message;

        RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}