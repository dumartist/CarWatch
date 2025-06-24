package com.example.carwatch.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carwatch.R;
import com.example.carwatch.databinding.FragmentHomeBinding;
import com.example.carwatch.services.ImageService;
import com.example.carwatch.ui.history.HistoryViewModel;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private HistoryViewModel historyViewModel;

    private Handler loadingTextHandler;
    private Runnable loadingTextRunnable;
    private int dotCount = 0;

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

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);

        homeViewModel.username.observe(getViewLifecycleOwner(), currentUsername -> {
            if (currentUsername != null && !currentUsername.isEmpty()) {
                binding.greetingText.setText("Hello " + currentUsername);
            } else {
                binding.greetingText.setText("Hello User");
            }
        });

        homeViewModel.imagePath.observe(getViewLifecycleOwner(), imagePath -> {
            if (imagePath != null) {
                displayImage(imagePath);
            }
        });

        historyViewModel.getHistoryUiItems().observe(getViewLifecycleOwner(), this::updateCarDetectionInfo);

        if (!homeViewModel.isImageFetched() || !homeViewModel.isHistoryFetched()) {
            showLoading(true);
        }

        fetchHistoryAndImage();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            homeViewModel.setImageFetched(false);
            homeViewModel.setHistoryFetched(false);
            fetchHistoryAndImage();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        homeViewModel.loadUsername();
    }

    private void updateCarDetectionInfo(List<HistoryViewModel.UiHistoryItem> uiHistoryItems) {
        if (uiHistoryItems != null && !uiHistoryItems.isEmpty()) {
            HistoryViewModel.UiHistoryItem latestItem = uiHistoryItems.get(0);

            binding.homeStatusTitle.setText(latestItem.getTitle());
            binding.homeCarNumber.setText(latestItem.getPlate());
            binding.lastDetectionTime.setText(getString(R.string.last_detection, latestItem.getTimestamp()));
            binding.homeDescription.setText(latestItem.getDetails());

            homeViewModel.setLastHistoryItems(uiHistoryItems);
            homeViewModel.setHistoryFetched(true);
        } else {
            binding.homeStatusTitle.setText("No data available");
            binding.homeCarNumber.setText("N/A");
            binding.lastDetectionTime.setText(getString(R.string.last_detection, "N/A"));
            binding.homeDescription.setText("N/A");
        }
    }

    private void fetchHistoryAndImage() {
        if (!homeViewModel.isImageFetched()) {
            fetchLatestImageFromAPI();
        } else {
            String cachedPath = homeViewModel.imagePath.getValue();
            if (cachedPath != null) {
                displayImage(cachedPath);
            }
            showLoading(false);
        }

        if (!homeViewModel.isHistoryFetched()) {
            historyViewModel.fetchAllHistoryData();
        } else {
            updateCarDetectionInfo(homeViewModel.getLastHistoryItems());
            showLoading(false);
        }
    }

    private void fetchLatestImageFromAPI() {
        ImageService.fetchAndSaveImage(getContext(), new ImageService.ImageFetchListener() {
            @Override
            public void onImageFetched(String imagePath) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        homeViewModel.setImagePath(imagePath);
                        homeViewModel.setImageFetched(true);
                        Toast.makeText(getContext(), "Image loaded successfully", Toast.LENGTH_SHORT).show();
                        binding.carImage.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onImageFetchError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        String errorMessage = "Failed to load image: " + error;
                        if (error.contains("failed to connect")) {
                            errorMessage += "\n\nPlease ensure:\n1. Flask server is running on port 8000\n2. Server is accessible from emulator";
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        binding.carImage.setVisibility(View.GONE);
                    });
                }
            }
        });
    }

    private void displayImage(String imagePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            options.inSampleSize = calculateInSampleSize(options, 800, 600);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            if (bitmap != null) {
                binding.carImage.setImageBitmap(bitmap);
                binding.carImage.setVisibility(View.VISIBLE);
            } else {
                binding.carImage.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Unable to load image", Toast.LENGTH_SHORT).show();
            }
        } catch (OutOfMemoryError e) {
            Log.e("HomeFragment", "OutOfMemoryError loading image: " + e.getMessage());
            binding.carImage.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Image too large to display", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("HomeFragment", "Error loading image: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.carImage.setVisibility(View.GONE);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.loadingText.setVisibility(View.VISIBLE);
            binding.carDetectionCard.setVisibility(View.GONE);
            binding.carImage.setVisibility(View.GONE);
            startLoadingTextAnimation();
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.loadingText.setVisibility(View.GONE);
            binding.carDetectionCard.setVisibility(View.VISIBLE);
            stopLoadingTextAnimation();
        }
    }

    private void startLoadingTextAnimation() {
        loadingTextHandler = new Handler(Looper.getMainLooper());
        loadingTextRunnable = new Runnable() {
            @Override
            public void run() {
                updateLoadingText();
                loadingTextHandler.postDelayed(this, 500);
            }
        };
        loadingTextHandler.post(loadingTextRunnable);
    }

    private void updateLoadingText() {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < dotCount; i++) {
            dots.append(".");
        }
        binding.loadingText.setText("Image is still processing" + dots);
        dotCount = (dotCount + 1) % 4;
    }

    private void stopLoadingTextAnimation() {
        if (loadingTextHandler != null) {
            loadingTextHandler.removeCallbacks(loadingTextRunnable);
        }
        dotCount = 0;
        binding.loadingText.setText("Image is still processing");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLoadingTextAnimation();

        if (binding != null && binding.carImage != null && binding.carImage.getDrawable() != null) {
            binding.carImage.setImageDrawable(null);
        }
        binding = null;
        if (loadingTextHandler != null) {
            loadingTextHandler.removeCallbacksAndMessages(null);
            loadingTextHandler = null;
        }
    }
}
