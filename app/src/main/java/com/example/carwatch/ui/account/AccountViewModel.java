package com.example.carwatch.ui.account;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.carwatch.model.ServerResponse;
import com.example.carwatch.network.ApiService;
import com.example.carwatch.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountViewModel extends AndroidViewModel {

    private final MutableLiveData<OperationStatus> operationStatus = new MutableLiveData<>(OperationStatus.IDLE);
    private final MutableLiveData<Boolean> usernameUpdateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> accountDeletionSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordUpdateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(false);


    private SharedPreferences sharedPreferences;
    private String userId;
    private ApiService apiService;

    public enum OperationStatus {
        IDLE, LOADING, SUCCESS, ERROR
    }

    public AccountViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences("my_app", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        apiService = RetrofitClient.getApiService(application.getApplicationContext());
    }

    public LiveData<OperationStatus> getOperationStatus() {
        return operationStatus;
    }

    public LiveData<Boolean> getUsernameUpdateSuccess() {
        return usernameUpdateSuccess;
    }

    public LiveData<Boolean> getAccountDeletionSuccess() {
        return accountDeletionSuccess;
    }

    public LiveData<Boolean> getPasswordUpdateSuccess() {
        return passwordUpdateSuccess;
    }

    public LiveData<Boolean> getLogoutSuccess() {
        return logoutSuccess;
    }

    public void resetUsernameUpdateSuccess() {
        usernameUpdateSuccess.setValue(false);
    }

    public void resetAccountDeletionSuccess() {
        accountDeletionSuccess.setValue(false);
    }

    public void resetPasswordUpdateSuccess() {
        passwordUpdateSuccess.setValue(false);
    }

    public void resetLogoutSuccess() {
        logoutSuccess.setValue(false);
    }


    public String getCurrentUsernameFromSharedPreferences() {
        if (sharedPreferences == null) {
            Log.e("AccountViewModel", "SharedPreferences not initialized.");
            return null;
        }
        return sharedPreferences.getString("username", null);
    }

    private String getCurrentPasswordFromSharedPreferences() {
        if (sharedPreferences == null) {
            Log.e("AccountViewModel", "SharedPreferences not initialized.");
            return null;
        }
        return sharedPreferences.getString("password", null);
    }

    public void updateName(String newName) {
        if (userId == null) {
            Log.e("AccountViewModel", "User ID is not set locally. Cannot update name.");
            operationStatus.setValue(OperationStatus.ERROR);
            Toast.makeText(getApplication(), "User not logged in. Cannot update name.", Toast.LENGTH_SHORT).show();
            return;
        }

        operationStatus.setValue(OperationStatus.LOADING);

        Map<String, String> body = new HashMap<>();
        body.put("new_username", newName);

        apiService.updateUser(body).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse.isSuccess()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", newName);
                        editor.apply();

                        usernameUpdateSuccess.setValue(true);
                        operationStatus.setValue(OperationStatus.SUCCESS);
                        String message = serverResponse.getMessage() != null ? serverResponse.getMessage() : "Username updated successfully";
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        operationStatus.setValue(OperationStatus.ERROR);
                        String message = serverResponse.getMessage() != null ? serverResponse.getMessage() : "Failed to update username";
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    operationStatus.setValue(OperationStatus.ERROR);
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown server error";
                        Log.e("UpdateUsername", "Server error: " + response.code() + " " + errorBody);
                        Toast.makeText(getApplication(), "Failed to update username. Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplication(), "Failed to update username. Server error.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("UpdateUsername", "Error: " + t.getMessage(), t);
                operationStatus.setValue(OperationStatus.ERROR);
                Toast.makeText(getApplication(), "Error connecting to server.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePassword(String currentPasswordInput, String newPassword) {
        if (userId == null) { // Client-side check
            Log.e("AccountViewModel", "User ID is not set locally. Cannot update password.");
            operationStatus.setValue(OperationStatus.ERROR);
            Toast.makeText(getApplication(), "User not logged in. Cannot update password.", Toast.LENGTH_SHORT).show();
            return;
        }

        operationStatus.setValue(OperationStatus.LOADING);

        Map<String, String> body = new HashMap<>();
        body.put("current_password", currentPasswordInput);
        body.put("new_password", newPassword);
        body.put("confirm_new_password", newPassword); // Flask expects confirm_new_password

        // userId is no longer sent as Flask uses session
        apiService.updatePassword(body).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse.isSuccess()) {
                        passwordUpdateSuccess.setValue(true);
                        operationStatus.setValue(OperationStatus.SUCCESS);
                        String message = serverResponse.getMessage() != null ? serverResponse.getMessage() : "Password updated successfully";
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        operationStatus.setValue(OperationStatus.ERROR);
                        String message = serverResponse.getMessage() != null ? serverResponse.getMessage() : "Failed to update password";
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    operationStatus.setValue(OperationStatus.ERROR);
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown server error";
                        Log.e("UpdatePassword", "Server error: " + response.code() + " " + errorBody);
                        Toast.makeText(getApplication(), "Failed to update password. Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplication(), "Failed to update password. Server error.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("UpdatePassword", "Error: " + t.getMessage(), t);
                operationStatus.setValue(OperationStatus.ERROR);
                Toast.makeText(getApplication(), "Error connecting to server.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteAccount(String currentPassword) {
        if (userId == null) { // Client-side check
            Log.e("AccountViewModel", "User ID is not set locally. Cannot delete account.");
            operationStatus.setValue(OperationStatus.ERROR);
            Toast.makeText(getApplication(), "User not logged in. Cannot delete account.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPassword.isEmpty()) {
            Toast.makeText(getApplication(), "Current password is required to delete account.", Toast.LENGTH_SHORT).show();
            operationStatus.setValue(OperationStatus.ERROR);
            return;
        }

        operationStatus.setValue(OperationStatus.LOADING);

        Map<String, String> body = new HashMap<>();
        body.put("password", currentPassword);

        // userId is no longer sent; Flask uses session. Pass currentPassword for verification.
        apiService.deleteUser(body).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse.isSuccess()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear(); // Clear all user data
                        editor.apply();

                        accountDeletionSuccess.setValue(true);
                        operationStatus.setValue(OperationStatus.SUCCESS);
                        String message = serverResponse.getMessage() != null ? serverResponse.getMessage() : "Account deleted successfully";
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        operationStatus.setValue(OperationStatus.ERROR);
                        String message = serverResponse.getMessage() != null ? serverResponse.getMessage() : "Failed to delete account";
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    operationStatus.setValue(OperationStatus.ERROR);
                     try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown server error";
                        Log.e("DeleteAccount", "Server error: " + response.code() + " " + errorBody);
                        Toast.makeText(getApplication(), "Failed to delete account. Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplication(), "Failed to delete account. Server error.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("DeleteAccount", "Error: " + t.getMessage(), t);
                operationStatus.setValue(OperationStatus.ERROR);
                Toast.makeText(getApplication(), "Error connecting to server.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void logout() {
        operationStatus.setValue(OperationStatus.LOADING);
        apiService.logout().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    clearLocalSessionData();
                    logoutSuccess.setValue(true);
                    operationStatus.setValue(OperationStatus.SUCCESS);
                    Toast.makeText(getApplication(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Logout", "Server logout failed or returned error. Proceeding with local logout.");
                    clearLocalSessionData();
                    logoutSuccess.setValue(true);
                    operationStatus.setValue(OperationStatus.ERROR);
                    String errorMessage = "Logout failed on server, but local session cleared.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    } else if (!response.isSuccessful()) {
                        errorMessage = "Server error: " + response.code();
                    }
                    Toast.makeText(getApplication(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("Logout", "Error connecting to server for logout: " + t.getMessage(), t);
                clearLocalSessionData();
                logoutSuccess.setValue(true);
                operationStatus.setValue(OperationStatus.ERROR);
                Toast.makeText(getApplication(), "Error connecting to server. Logged out locally.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearLocalSessionData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("isLoggedIn");
        editor.remove("userId");
        editor.remove("username");
        editor.remove("password");
        editor.apply();
    }
}