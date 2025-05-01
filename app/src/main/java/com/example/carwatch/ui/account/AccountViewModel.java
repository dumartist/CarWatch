package com.example.carwatch.ui.account;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AccountViewModel extends AndroidViewModel {

    private final MutableLiveData<OperationStatus> operationStatus = new MutableLiveData<>(OperationStatus.IDLE);
    private final MutableLiveData<Boolean> usernameUpdateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> accountDeletionSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordUpdateSuccess = new MutableLiveData<>(false);

    private SharedPreferences sharedPreferences;
    private String userId;

    private static final String UPDATE_USERNAME_URL = "http://192.168.1.4/carwatch/users_update.php";
    private static final String UPDATE_PASSWORD_URL = "http://192.168.1.4/carwatch/update_password.php";
    private static final String DELETE_ACCOUNT_URL = "http://192.168.1.4/carwatch/delete_user.php";

    public enum OperationStatus {
        IDLE, LOADING, SUCCESS, ERROR
    }

    public AccountViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences("my_app", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            Log.e("AccountViewModel", "User ID not found in SharedPreferences");
        }
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

    public void resetUsernameUpdateSuccess() {
        usernameUpdateSuccess.setValue(false);
    }

    public void resetAccountDeletionSuccess() {
        accountDeletionSuccess.setValue(false);
    }

    public void resetPasswordUpdateSuccess() {
        passwordUpdateSuccess.setValue(false);
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
            Log.e("AccountViewModel", "User ID is not set. Cannot update name.");
            operationStatus.setValue(OperationStatus.ERROR);
            Toast.makeText(getApplication(), "User not logged in. Cannot update name.", Toast.LENGTH_SHORT).show();
            return;
        }

        operationStatus.setValue(OperationStatus.LOADING);
        new UpdateUsernameTask(userId, newName).execute();
    }

    public void updatePassword(String currentPasswordInput, String newPassword) {
        if (userId == null) {
            Log.e("AccountViewModel", "User ID is not set. Cannot update password.");
            operationStatus.setValue(OperationStatus.ERROR);
            Toast.makeText(getApplication(), "User not logged in. Cannot update password.", Toast.LENGTH_SHORT).show();
            return;
        }

        operationStatus.setValue(OperationStatus.LOADING);
        new UpdatePasswordTask(userId, currentPasswordInput, newPassword, newPassword).execute();
    }

    public void deleteAccount() {
        if (userId == null) {
            Log.e("AccountViewModel", "User ID is not set. Cannot delete account.");
            operationStatus.setValue(OperationStatus.ERROR);
            Toast.makeText(getApplication(), "User not logged in. Cannot delete account.", Toast.LENGTH_SHORT).show();
            return;
        }

        operationStatus.setValue(OperationStatus.LOADING);
        new DeleteAccountTask(userId).execute();
    }

    // --- AsyncTasks for Server Communication ---
    private class UpdateUsernameTask extends AsyncTask<Void, Void, String> {
        private final String userId;
        private final String newUsername;

        UpdateUsernameTask(String userId, String newUsername) {
            this.userId = userId;
            this.newUsername = newUsername;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(UPDATE_USERNAME_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8") + "&"
                        + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(newUsername, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;

            } catch (IOException e) {
                Log.e("UpdateUsernameTask", "Error in background task", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("UpdateUsernameTask", "Result: " + result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.getBoolean("success")) {
                        // Update SharedPreferences with the new username (username is not sensitive like a password)
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", newUsername);
                        editor.apply();

                        usernameUpdateSuccess.setValue(true);
                        operationStatus.setValue(OperationStatus.SUCCESS);
                        Toast.makeText(getApplication(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        operationStatus.setValue(OperationStatus.ERROR);
                        String message = jsonResponse.optString("message", "Failed to update username");
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("UpdateUsernameTask", "JSON parsing error", e);
                    operationStatus.setValue(OperationStatus.ERROR);
                    Toast.makeText(getApplication(), "Invalid response from server", Toast.LENGTH_SHORT).show();
                }
            } else {
                operationStatus.setValue(OperationStatus.ERROR);
                Toast.makeText(getApplication(), "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdatePasswordTask extends AsyncTask<Void, Void, String> {
        private final String userId;
        private final String currentPassword;
        private final String newPassword;
        private final String confirmNewPassword;

        UpdatePasswordTask(String userId, String currentPassword, String newPassword, String confirmNewPassword) {
            this.userId = userId;
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
            this.confirmNewPassword = confirmNewPassword;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(UPDATE_PASSWORD_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8") + "&"
                        + URLEncoder.encode("current_password", "UTF-8") + "=" + URLEncoder.encode(currentPassword, "UTF-8") + "&"
                        + URLEncoder.encode("new_password", "UTF-8") + "=" + URLEncoder.encode(newPassword, "UTF-8") + "&"
                        + URLEncoder.encode("confirm_new_password", "UTF-8") + "=" + URLEncoder.encode(confirmNewPassword, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;

            } catch (IOException e) {
                Log.e("UpdatePasswordTask", "Error in background task", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("UpdatePasswordTask", "Result: " + result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.getBoolean("success")) {
                        passwordUpdateSuccess.setValue(true);
                        operationStatus.setValue(OperationStatus.SUCCESS);
                        Toast.makeText(getApplication(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        operationStatus.setValue(OperationStatus.ERROR);
                        String message = jsonResponse.optString("message", "Failed to update password");
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("UpdatePasswordTask", "JSON parsing error", e);
                    operationStatus.setValue(OperationStatus.ERROR);
                    Toast.makeText(getApplication(), "Invalid response from server", Toast.LENGTH_SHORT).show();
                }
            } else {
                operationStatus.setValue(OperationStatus.ERROR);
                Toast.makeText(getApplication(), "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteAccountTask extends AsyncTask<Void, Void, String> {
        private final String userId;

        DeleteAccountTask(String userId) {
            this.userId = userId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(DELETE_ACCOUNT_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;

            } catch (IOException e) {
                Log.e("DeleteAccountTask", "Error in background task", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("DeleteAccountTask", "Result: " + result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.getBoolean("success")) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        accountDeletionSuccess.setValue(true);
                        operationStatus.setValue(OperationStatus.SUCCESS);
                        Toast.makeText(getApplication(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        operationStatus.setValue(OperationStatus.ERROR);
                        String message = jsonResponse.optString("message", "Failed to delete account");
                        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("DeleteAccountTask", "JSON parsing error", e);
                    operationStatus.setValue(OperationStatus.ERROR);
                    Toast.makeText(getApplication(), "Invalid response from server", Toast.LENGTH_SHORT).show();
                }
            } else {
                operationStatus.setValue(OperationStatus.ERROR);
                Toast.makeText(getApplication(), "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        }
    }
}