package com.example.carwatch.ui.history;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryViewModel extends AndroidViewModel {

    public static class HistoryItem {
        private String title;
        private String timestamp;
        private String details;
        private String date;
        private String plate;

        public HistoryItem(String title, String timestamp, String details, String date, String plate) {
            this.title = title;
            this.timestamp = timestamp;
            this.details = details;
            this.date = date;
            this.plate = plate;
        }

        // Getters
        public String getTitle() { return title; }
        public String getTimestamp() { return timestamp; }
        public String getDetails() { return details; }
        public String getDate() { return date; }
        public String getPlate() { return plate; }
    }

    private MutableLiveData<List<HistoryItem>> historyItems = new MutableLiveData<>();
    private SharedPreferences sharedPreferences;
    private String userId;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences("my_app", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        if (userId != null) {
            fetchHistoryData(userId);
        } else {
            Log.e("HistoryViewModel", "UserId not found in SharedPreferences");
            historyItems.setValue(new ArrayList<>());
        }
    }

    public LiveData<List<HistoryItem>> getHistoryItems() {
        return historyItems;
    }

    public String getUserId() {
        return userId;
    }

    public void fetchHistoryData(String userId) {
        new FetchHistoryTask().execute(userId);
    }

    private class FetchHistoryTask extends AsyncTask<String, Void, List<HistoryItem>> {
        @Override
        protected List<HistoryItem> doInBackground(String... params) {
            String userId = params[0];
            List<HistoryItem> result = new ArrayList<>();

            try {
                URL url = new URL("http://192.168.1.4/carwatch/history_select.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                String postData = "user_id=" + userId;
                byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(postDataBytes);
                wr.flush();
                wr.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray data = jsonResponse.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject historyObject = data.getJSONObject(i);
                                String subject = historyObject.getString("subject");
                                String plate = historyObject.getString("plate");
                                String description = historyObject.getString("description");
                                String rawDate = historyObject.getString("date");
                                Log.d("FetchHistoryTask", "Raw Date from DB: " + rawDate);

                                // Format the date to dd-MM-yyyy hh:mm a
                                String formattedDate = formatDate(rawDate);

                                HistoryItem historyItem = new HistoryItem(subject, formattedDate, description, extractDate(formattedDate), plate);
                                result.add(historyItem);
                            }
                        } else {
                            Log.e("FetchHistoryTask", "Error: " + jsonResponse.getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e("FetchHistoryTask", "JSON Parsing Error: " + e.getMessage());
                    }
                } else {
                    Log.e("FetchHistoryTask", "HTTP Error: " + responseCode);
                }

                conn.disconnect();

            } catch (IOException e) {
                Log.e("FetchHistoryTask", "Network Error: " + e.getMessage());
            }

            return result;
        }


        private String formatDate(String rawDate) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US);
                Date date = inputFormat.parse(rawDate);
                return outputFormat.format(date);
            } catch (ParseException e) {
                Log.e("FetchHistoryTask", "Date Parsing Error: " + e.getMessage());
                return "N/A";
            }
        }

        private String extractDate(String formattedDate){
            try{
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                Date date = inputFormat.parse(formattedDate);
                return outputFormat.format(date);
            }catch (ParseException e) {
                Log.e("FetchHistoryTask", "Date Parsing Error: " + e.getMessage());
                return "N/A";
            }
        }


        @Override
        protected void onPostExecute(List<HistoryItem> historyItems) {
            HistoryViewModel.this.historyItems.setValue(historyItems);
            saveHistoryData(historyItems);
        }

        private void saveHistoryData(List<HistoryItem> historyItems) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // Clear old data
            editor.remove("history_data");

            // Convert historyItems to JSON and save
            JSONArray jsonArray = new JSONArray();
            for (HistoryItem item : historyItems) {
                JSONObject jsonItem = new JSONObject();
                try {
                    jsonItem.put("title", item.getTitle());
                    jsonItem.put("timestamp", item.getTimestamp());
                    jsonItem.put("details", item.getDetails());
                    jsonItem.put("date", item.getDate());
                    jsonItem.put("plate", item.getPlate());
                    jsonArray.put(jsonItem);
                } catch (JSONException e) {
                    Log.e("HistoryViewModel", "JSON Encode Error: " + e.getMessage());
                }
            }
            editor.putString("history_data", jsonArray.toString());
            editor.apply();
        }
    }

    public void clearHistory() {
        historyItems.setValue(new ArrayList<>());
    }

    private List<HistoryItem> loadHistoryDataFromSharedPreferences() {
        List<HistoryItem> historyItems = new ArrayList<>();
        String historyDataJson = sharedPreferences.getString("history_data", null);

        if (historyDataJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(historyDataJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonItem = jsonArray.getJSONObject(i);
                    String title = jsonItem.getString("title");
                    String timestamp = jsonItem.getString("timestamp");
                    String details = jsonItem.getString("details");
                    String date = jsonItem.getString("date");
                    String plate = jsonItem.getString("plate");

                    HistoryItem historyItem = new HistoryItem(title, timestamp, details, date, plate);
                    historyItems.add(historyItem);
                }
            } catch (JSONException e) {
                Log.e("HistoryViewModel", "JSON Decode Error: " + e.getMessage());
            }
        }
        return historyItems;
    }

    public void filterHistoryByDate(String selectedDate) {
        List<HistoryItem> currentItems = historyItems.getValue();

        List<HistoryItem> sharedPrefItems = loadHistoryDataFromSharedPreferences();

        // If SharedPreferences has data, use it; otherwise, fetch from the server
        if (!sharedPrefItems.isEmpty()) {
            currentItems = sharedPrefItems;
        } else {
            fetchHistoryData(userId);
            return; // Return after fetching data
        }

        List<HistoryItem> filteredItems = new ArrayList<>();
        for (HistoryItem item : currentItems) {
            if (item.getDate() != null && item.getDate().equals(selectedDate)) {
                filteredItems.add(item);
            }
        }
        historyItems.setValue(filteredItems);
    }
}