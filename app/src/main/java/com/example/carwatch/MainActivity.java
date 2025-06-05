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

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginSuccessListener, AccountFragment.AccountDeletionNavigator, AccountFragment.LogoutNavigator {

    private ActivityMainBinding binding;
    private boolean isLoggedIn = false;
    private SharedPreferences sharedPreferences;

    @Override
    public void onLoginSuccess(String userId, String username) {
        isLoggedIn = true;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", userId);
        editor.putString("username", username);
        editor.apply();

        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        replaceFragment(new HomeFragment());
    }

    @Override
    public void onAccountDeletionSuccessNavigation() {
        isLoggedIn = false;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        navigateToLoginFragment();
    }

    @Override
    public void onLogoutSuccessNavigation() {
        isLoggedIn = false;
        navigateToLoginFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("my_app", Context.MODE_PRIVATE);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        navigateToLoginFragment();

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
        if (fragment == null) return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void navigateToLoginFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new LoginFragment());
        fragmentTransaction.commit();

        binding.bottomNavigationView.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isLoggedIn", isLoggedIn);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isLoggedIn = savedInstanceState.getBoolean("isLoggedIn", false);
    }
}