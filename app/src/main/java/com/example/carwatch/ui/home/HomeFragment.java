package com.example.carwatch.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.carwatch.R;
import com.example.carwatch.databinding.FragmentHomeBinding;
import com.example.carwatch.ui.history.HistoryViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private HistoryViewModel historyViewModel;
    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class); // Initialize HistoryViewModel

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("my_app", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "User"); // Default value if not found

        String greeting = "Hello " + username;
        binding.greetingText.setText(greeting);

        SwitchMaterial lampSwitch = binding.lampToggleSwitch;
        lampSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Implement actual lamp control logic later on
        });

        historyViewModel.getHistoryItems().observe(getViewLifecycleOwner(), historyItems -> {
            updateCarDetectionInfo(historyItems);
        });

        boolean hasNewNotifications = false;
        if (hasNewNotifications) {
            binding.notificationIcon.setVisibility(View.VISIBLE);
        } else {
            binding.notificationIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void updateCarDetectionInfo(List<HistoryViewModel.HistoryItem> historyItems) {
        TextView homeStatusTitle = binding.homeStatusTitle;
        TextView homeCarNumber = binding.homeCarNumber;
        TextView lastDetectionTime = binding.lastDetectionTime;
        TextView homeDescription = binding.homeDescription;

        if (historyItems != null && !historyItems.isEmpty()) {
            // Get the latest history item
            HistoryViewModel.HistoryItem latestItem = historyItems.get(0);

            // Update UI elements with data from the latest history item
            homeStatusTitle.setText(latestItem.getTitle());
            homeCarNumber.setText(latestItem.getPlate());

            // Format the last detection time
            String formattedTime = latestItem.getTimestamp();
            lastDetectionTime.setText(getString(R.string.last_detection, formattedTime));

            // Set the license plate
            homeDescription.setText(latestItem.getDetails());
        } else {
            // Handle the case where there is no history data
            homeStatusTitle.setText("No data available");
            homeCarNumber.setText("N/A");
            lastDetectionTime.setText(getString(R.string.last_detection, "N/A"));
            homeDescription.setText("N/A");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}