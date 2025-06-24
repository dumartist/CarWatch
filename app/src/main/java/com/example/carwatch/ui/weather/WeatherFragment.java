package com.example.carwatch.ui.weather;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.carwatch.R;
import com.example.carwatch.databinding.FragmentWeatherBinding;

public class WeatherFragment extends Fragment {
    private FragmentWeatherBinding binding;
    private WeatherViewModel weatherViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Context themedContext = new ContextThemeWrapper(requireContext(), R.style.AppTheme);
        LayoutInflater themedInflater = inflater.cloneInContext(themedContext);

        binding = FragmentWeatherBinding.inflate(themedInflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        setupObservers();
        setupButtonListener();

        weatherViewModel.fetchWeatherData("Jakarta");
    }

    private void setupObservers() {
        weatherViewModel.weatherData.observe(getViewLifecycleOwner(), weatherData -> {
            binding.cityNameText.setText(weatherData.getCityName());
            binding.temperatureText.setText(String.format("%.0f°", weatherData.getTemperature()));
            binding.humidityText.setText(String.format("%.0f%%", weatherData.getHumidity()));
            binding.windText.setText(String.format("%.0f km/h", weatherData.getWindSpeed()));
            binding.descriptionText.setText(weatherData.getDescription());

            String resourceName = "ic_" + weatherData.getIconCode();
            int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                binding.weatherIcon.setImageResource(resId);
            }
        });

        weatherViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonListener() {
        binding.fetchWeatherButton.setOnClickListener(v -> {
            String cityName = binding.cityNameInput.getText().toString().trim();
            if (!cityName.isEmpty()) {
                weatherViewModel.fetchWeatherData(cityName);
                binding.cityNameInput.setText("");
            } else {
                binding.cityNameInput.setError("Please enter a city name");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
