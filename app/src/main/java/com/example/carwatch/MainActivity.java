package com.example.carwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.carwatch.databinding.ActivityMainBinding;
import com.example.carwatch.ui.account.AccountFragment;
import com.example.carwatch.ui.history.HistoryFragment;
import com.example.carwatch.ui.home.HomeFragment;
import com.example.carwatch.ui.login.LoginFragment;
import com.example.carwatch.ui.weather.WeatherFragment;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginSuccessListener, AccountFragment.AccountDeletionNavigator {

    private ActivityMainBinding binding;
    private boolean isLoggedIn = false;
    private SharedPreferences sharedPreferences;

    @Override
    public void onLoginSuccess(String username) {
        isLoggedIn = true;

        // Store login state and username in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();

        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        replaceFragment(new HomeFragment());
    }

    @Override
    public void onAccountDeletionSuccessNavigation() {
        // Clear login state
        isLoggedIn = false;

        // Clear all user data from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate back to the Login Fragment and clear the back stack
        navigateToLoginFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("my_app", Context.MODE_PRIVATE);

        // Inflate the binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve login state from SharedPreferences
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            navigateToLoginFragment();
        } else {
            replaceFragment(new HomeFragment());
            binding.bottomNavigationView.setVisibility(View.VISIBLE);
        }

        // Set up bottom navigation listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.weather) {
                selectedFragment = new WeatherFragment();
            } else if (itemId == R.id.history) {
                selectedFragment = new HistoryFragment();
            } else if (itemId == R.id.account) {
                // AccountFragment should be accessible only if logged in
                if (isLoggedIn) {
                    selectedFragment = new AccountFragment();
                } else {
                    navigateToLoginFragment();
                    return false;
                }
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }

            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        // Null check for safety
        if (fragment == null) return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the current fragment in frame_layout
        fragmentTransaction.replace(R.id.frame_layout, fragment);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    private void navigateToLoginFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Clear the fragment back stack completely
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Replace with LoginFragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new LoginFragment());
        fragmentTransaction.commit();

        // Hide BottomNavigationView
        binding.bottomNavigationView.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save login state
        outState.putBoolean("isLoggedIn", isLoggedIn);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isLoggedIn = savedInstanceState.getBoolean("isLoggedIn", false);
    }
}