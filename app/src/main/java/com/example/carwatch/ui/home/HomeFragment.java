package com.example.carwatch.ui.home;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        homeViewModel.username.observe(getViewLifecycleOwner(), currentUsername -> {
            if (currentUsername != null && !currentUsername.isEmpty()) {
                String greeting = "Hello " + currentUsername;
                binding.greetingText.setText(greeting);
            } else {
                binding.greetingText.setText("Hello User");
            }
        });

        SwitchMaterial lampSwitch = binding.lampToggleSwitch;
        lampSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Implement actual lamp control logic
        });

        historyViewModel.getHistoryUiItems().observe(getViewLifecycleOwner(), this::updateCarDetectionInfo);

        historyViewModel.fetchAllHistoryData();
    }

    @Override
    public void onResume() {
        super.onResume();
        homeViewModel.loadUsername();
    }

    private void updateCarDetectionInfo(List<HistoryViewModel.UiHistoryItem> uiHistoryItems) {
        TextView homeStatusTitle = binding.homeStatusTitle;
        TextView homeCarNumber = binding.homeCarNumber;
        TextView lastDetectionTime = binding.lastDetectionTime;
        TextView homeDescription = binding.homeDescription;

        if (uiHistoryItems != null && !uiHistoryItems.isEmpty()) {
            HistoryViewModel.UiHistoryItem latestItem = uiHistoryItems.get(0);

            homeStatusTitle.setText(latestItem.getTitle());
            homeCarNumber.setText(latestItem.getPlate());
            String formattedTime = latestItem.getTimestamp();
            lastDetectionTime.setText(getString(R.string.last_detection, formattedTime));
            homeDescription.setText(latestItem.getDetails());
        } else {
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