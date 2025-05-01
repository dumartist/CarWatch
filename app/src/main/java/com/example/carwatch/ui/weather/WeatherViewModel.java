package com.example.carwatch.ui.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherViewModel extends ViewModel {
    private static final String API_KEY = "8a5e4a7705ac1a2528fcaedea2e95b26"; // Replace with your actual API key

    private final MutableLiveData<WeatherData> _weatherData = new MutableLiveData<>();
    public LiveData<WeatherData> weatherData = _weatherData;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public void fetchWeatherData(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric";

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                processWeatherData(result);
            } catch (IOException e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }

    private void processWeatherData(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject main = jsonObject.getJSONObject("main");

            double temperature = main.getDouble("temp");
            double humidity = main.getDouble("humidity");
            double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");

            String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            String iconCode = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
            String cityName = jsonObject.getString("name");

            WeatherData data = new WeatherData(
                    cityName,
                    temperature,
                    humidity,
                    windSpeed,
                    description,
                    iconCode
            );

            _weatherData.postValue(data);
        } catch (JSONException e) {
            _errorMessage.postValue(e.getMessage());
        }
    }

    // Inner class to hold weather data
    public static class WeatherData {
        private final String cityName;
        private final double temperature;
        private final double humidity;
        private final double windSpeed;
        private final String description;
        private final String iconCode;

        public WeatherData(String cityName, double temperature, double humidity,
                           double windSpeed, String description, String iconCode) {
            this.cityName = cityName;
            this.temperature = temperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.description = description;
            this.iconCode = iconCode;
        }

        // Getters
        public String getCityName() { return cityName; }
        public double getTemperature() { return temperature; }
        public double getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public String getDescription() { return description; }
        public String getIconCode() { return iconCode; }
    }
}
