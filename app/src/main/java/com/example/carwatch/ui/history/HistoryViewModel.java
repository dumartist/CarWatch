package com.example.carwatch.ui.history;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.example.carwatch.model.HistoryData;
import com.example.carwatch.model.HistoryResponse;
import com.example.carwatch.network.ApiService;
import com.example.carwatch.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryViewModel extends AndroidViewModel {

    public static class UiHistoryItem {
        private final String title;
        private final String timestamp;
        private final String details;
        private final String dateOnly;
        private final String plate;

        public UiHistoryItem(String title, String timestamp, String details, String dateOnly, String plate) {
            this.title = title;
            this.timestamp = timestamp;
            this.details = details;
            this.dateOnly = dateOnly;
            this.plate = (plate != null && !plate.isEmpty()) ? plate : "License not Detected";
        }

        public String getTitle() { return title; }
        public String getTimestamp() { return timestamp; }
        public String getDetails() { return details; }
        public String getDateOnly() { return dateOnly; }
        public String getPlate() { return plate; }
    }

    private final MutableLiveData<List<UiHistoryItem>> historyUiItems = new MutableLiveData<>();
    private List<HistoryData> allFetchedHistoryData = new ArrayList<>();
    private final ApiService apiService;
    private final String userId;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService(application.getApplicationContext());
        SharedPreferences sharedPreferences = application.getSharedPreferences("my_app", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Log.e("HistoryViewModel", "UserId not found in SharedPreferences. Cannot fetch history.");
            historyUiItems.setValue(new ArrayList<>());
        }
    }

    public LiveData<List<UiHistoryItem>> getHistoryUiItems() {
        return historyUiItems;
    }

    public void fetchAllHistoryData() {
        if (userId == null) {
            Toast.makeText(getApplication(), "User not logged in. Cannot fetch history.", Toast.LENGTH_SHORT).show();
            historyUiItems.setValue(new ArrayList<>());
            return;
        }

        apiService.getHistory().enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistoryResponse> call, @NonNull Response<HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HistoryResponse historyResponse = response.body();
                    if (historyResponse.isSuccess() && historyResponse.getData() != null) {
                        allFetchedHistoryData = historyResponse.getData();
                        historyUiItems.setValue(convertToUiItems(allFetchedHistoryData));
                    } else {
                        Log.e("HistoryViewModel", "Fetching history failed: " + historyResponse.getMessage());
                        Toast.makeText(getApplication(), "Failed to load history: " + historyResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        historyUiItems.setValue(new ArrayList<>());
                    }
                } else {
                    Log.e("HistoryViewModel", "Fetching history error: " + response.code());
                    Toast.makeText(getApplication(), "Error loading history: " + response.code(), Toast.LENGTH_SHORT).show();
                    historyUiItems.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistoryResponse> call, @NonNull Throwable t) {
                Log.e("HistoryViewModel", "Network error fetching history: " + t.getMessage(), t);
                Toast.makeText(getApplication(), "Network error. Could not load history.", Toast.LENGTH_SHORT).show();
                historyUiItems.setValue(new ArrayList<>());
            }
        });
    }

    private List<UiHistoryItem> convertToUiItems(List<HistoryData> historyDataList) {
        List<UiHistoryItem> uiItems = new ArrayList<>();
        for (HistoryData data : historyDataList) {
            String formattedTimestamp = formatRawDateToTimestamp(data.getDate());
            String dateOnly = formatRawDateToDateOnly(data.getDate());
            uiItems.add(new UiHistoryItem(data.getSubject(), formattedTimestamp, data.getDescription(), dateOnly, data.getPlate()));
        }
        return uiItems;
    }

    private String formatRawDateToTimestamp(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            Date date = inputFormat.parse(rawDate);
            
            long correctedTime = date.getTime() - (7 * 60 * 60 * 1000);
            Date correctedDate = new Date(correctedTime);
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MM yyyy HH:mm:ss", Locale.US);
            outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
            
            return outputFormat.format(correctedDate);
        } catch (ParseException e) {
            Log.e("HistoryViewModel", "Timestamp Parsing Error for date '" + rawDate + "': " + e.getMessage());
            return rawDate;
        }
    }

    private String formatRawDateToDateOnly(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            Date date = inputFormat.parse(rawDate);
            
            long correctedTime = date.getTime() - (7 * 60 * 60 * 1000);
            Date correctedDate = new Date(correctedTime);
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
            
            return outputFormat.format(correctedDate);
        } catch (ParseException e) {
            Log.e("HistoryViewModel", "DateOnly Parsing Error for date '" + rawDate + "': " + e.getMessage());
            if (rawDate != null && rawDate.contains("-")) {
                try {
                    String datePart = rawDate.substring(0, 10);
                    SimpleDateFormat inputFormatSimple = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    inputFormatSimple.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

                    SimpleDateFormat outputFormatSimple = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    outputFormatSimple.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta")); 

                    Date simpleDate = inputFormatSimple.parse(datePart);
                    return outputFormatSimple.format(simpleDate);
                } catch (ParseException | StringIndexOutOfBoundsException ex) {
                    Log.e("HistoryViewModel", "Fallback DateOnly Parsing Error for date '" + rawDate + "': " + ex.getMessage());
                }
            }
            return "N/A";
        }
    }

    public void filterHistoryByDate(String selectedDate) {
        if (allFetchedHistoryData.isEmpty()) {
            apiService.getHistory().enqueue(new Callback<HistoryResponse>() {
                @Override
                public void onResponse(@NonNull Call<HistoryResponse> call, @NonNull Response<HistoryResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        allFetchedHistoryData = response.body().getData();
                        applyFilter(selectedDate);
                    } else {
                        Toast.makeText(getApplication(), "Could not load history to filter.", Toast.LENGTH_SHORT).show();
                        historyUiItems.setValue(new ArrayList<>());
                    }
                }
                @Override
                public void onFailure(@NonNull Call<HistoryResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getApplication(), "Network error. Could not filter history.", Toast.LENGTH_SHORT).show();
                    historyUiItems.setValue(new ArrayList<>());
                }
            });
        } else {
            applyFilter(selectedDate);
        }
    }

    private void applyFilter(String selectedDate) {
        List<UiHistoryItem> filteredUiItems = allFetchedHistoryData.stream()
                .map(data -> {
                    String formattedTimestamp = formatRawDateToTimestamp(data.getDate());
                    String dateOnly = formatRawDateToDateOnly(data.getDate());
                    return new UiHistoryItem(data.getSubject(), formattedTimestamp, data.getDescription(), dateOnly, data.getPlate());
                })
                .filter(uiItem -> uiItem.getDateOnly().equals(selectedDate))
                .collect(Collectors.toList());
        historyUiItems.setValue(filteredUiItems);
    }

    public void clearDisplayedHistory() {
        historyUiItems.setValue(new ArrayList<>());
    }
}