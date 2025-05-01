package com.example.carwatch.ui.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.carwatch.R;
import com.example.carwatch.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private AccountViewModel viewModel;
    private FragmentAccountBinding binding;
    private SharedPreferences sharedPreferences;

    private AccountDeletionNavigator accountDeletionNavigator;

    public interface AccountDeletionNavigator {
        void onAccountDeletionSuccessNavigation();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if the hosting Activity implements the required interface
        if (context instanceof AccountDeletionNavigator) {
            accountDeletionNavigator = (AccountDeletionNavigator) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AccountDeletionNavigator");
        }
        // Initialize SharedPreferences here, primarily for theme preference
        sharedPreferences = requireActivity().getSharedPreferences("my_app", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context themedContext = new ContextThemeWrapper(requireContext(), R.style.AppTheme);
        LayoutInflater themedInflater = inflater.cloneInContext(themedContext);

        binding = FragmentAccountBinding.inflate(themedInflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        // Setup UI components and Observers
        setupUserInfoDisplay();
        setupObservers();
        setupListeners();
        setupThemeSwitch();
    }

    // Get current username from ViewModel and update the TextView
    private void setupUserInfoDisplay() {
        // Access the username via the ViewModel, which gets it from its SharedPreferences instance
        String username = viewModel.getCurrentUsernameFromSharedPreferences();
        if (username != null) {
            binding.tvName.setText(username);
        } else {
            binding.tvName.setText("User");
        }
    }

    private void setupObservers() {
        // Observe operation status
        viewModel.getOperationStatus().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case LOADING:
                    Log.d("AccountFragment", "Operation Status: LOADING");
                    break;
                case SUCCESS:
                    // Hide loading indicator
                    Log.d("AccountFragment", "Operation Status: SUCCESS");
                    break;
                case ERROR:
                    // Hide loading indicator and show generic error
                    Log.d("AccountFragment", "Operation Status: ERROR");
                    break;
                case IDLE:
                    // Initial state or after an operation completes
                    Log.d("AccountFragment", "Operation Status: IDLE");
                    break;
            }
        });

        // Observe username update success event
        viewModel.getUsernameUpdateSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                setupUserInfoDisplay();
                viewModel.resetUsernameUpdateSuccess();
            }
        });

        // Observe password update success event
        viewModel.getPasswordUpdateSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                clearPasswordFields();
                viewModel.resetPasswordUpdateSuccess();
            }
        });


        // Observe account deletion success event
        viewModel.getAccountDeletionSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                if (accountDeletionNavigator != null) {
                    accountDeletionNavigator.onAccountDeletionSuccessNavigation();
                }
                viewModel.resetAccountDeletionSuccess();
            }
        });
    }

    private void setupListeners() {
        binding.btnUpdateName.setOnClickListener(v -> {
            String newName = binding.etNewName.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.updateName(newName);
            } else {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnUpdatePassword.setOnClickListener(v -> {
            String currentPass = binding.etCurrentPassword.getText().toString();
            String newPass = binding.etNewPassword.getText().toString();
            String confirmPass = binding.etConfirmPassword.getText().toString();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), "All password fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 8) {
                Toast.makeText(requireContext(), "New password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.updatePassword(currentPass, newPass);
        });

        binding.btnRemoveAccount.setOnClickListener(v -> {
            showAccountRemovalConfirmation();
        });
    }

    private void setupThemeSwitch() {
        // Set initial switch state
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        binding.switchDarkMode.setChecked(isDarkModeEnabled);

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
        });
    }

    private void showAccountRemovalConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Account")
                .setMessage("Are you sure you want to remove your account? This action is irreversible.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    viewModel.deleteAccount();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_dialog_alert)
                .show();
    }

    private void clearPasswordFields() {
        binding.etCurrentPassword.setText("");
        binding.etNewPassword.setText("");
        binding.etConfirmPassword.setText("");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear the reference to the navigator to prevent leaks
        accountDeletionNavigator = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear binding to avoid memory leaks
        binding = null;
    }
}