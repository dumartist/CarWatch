package com.example.carwatch.ui.account;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    private AccountDeletionNavigator accountDeletionNavigator;
    private LogoutNavigator logoutNavigator;

    public interface AccountDeletionNavigator {
        void onAccountDeletionSuccessNavigation();
    }

    public interface LogoutNavigator {
        void onLogoutSuccessNavigation();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AccountDeletionNavigator) {
            accountDeletionNavigator = (AccountDeletionNavigator) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AccountDeletionNavigator");
        }
        if (context instanceof LogoutNavigator) {
            logoutNavigator = (LogoutNavigator) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LogoutNavigator");
        }
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

        setupUserInfoDisplay();
        setupObservers();
        setupListeners();
    }

    private void setupUserInfoDisplay() {
        String username = viewModel.getCurrentUsernameFromSharedPreferences();
        if (username != null) {
            binding.tvName.setText(username);
        } else {
            binding.tvName.setText("User");
        }
    }

    private void setupObservers() {
        viewModel.getOperationStatus().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case LOADING:
                    Log.d("AccountFragment", "Operation Status: LOADING");
                    break;
                case SUCCESS:
                    Log.d("AccountFragment", "Operation Status: SUCCESS");
                    break;
                case ERROR:
                    Log.d("AccountFragment", "Operation Status: ERROR");
                    break;
                case IDLE:
                    Log.d("AccountFragment", "Operation Status: IDLE");
                    break;
            }
        });

        viewModel.getUsernameUpdateSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                setupUserInfoDisplay();
                viewModel.resetUsernameUpdateSuccess();
            }
        });

        viewModel.getPasswordUpdateSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                clearPasswordFields();
                viewModel.resetPasswordUpdateSuccess();
            }
        });

        viewModel.getAccountDeletionSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                if (accountDeletionNavigator != null) {
                    accountDeletionNavigator.onAccountDeletionSuccessNavigation();
                }
                viewModel.resetAccountDeletionSuccess();
            }
        });

        viewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                if (logoutNavigator != null) {
                    logoutNavigator.onLogoutSuccessNavigation();
                }
                viewModel.resetLogoutSuccess();
            }
        });
    }

    private void setupListeners() {
        binding.btnUpdateName.setOnClickListener(v -> {
            String newName = binding.etNewName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.updateName(newName);
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

        binding.btnLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
    }

    private void showAccountRemovalConfirmation() {
        showPasswordConfirmationDialog("Remove Account",
                "Are you sure you want to remove your account? This action is irreversible. Enter current password to confirm.",
                currentPassword -> {
                    if (currentPassword != null && !currentPassword.isEmpty()) {
                        viewModel.deleteAccount(currentPassword);
                    } else {
                        Toast.makeText(requireContext(), "Current password is required to delete account.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPasswordConfirmationDialog(String title, String message, PasswordConfirmationListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);
        builder.setMessage(message);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Current Password");
        builder.setView(input);

        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            listener.onPasswordConfirmed(input.getText().toString());
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {
            dialog.cancel();
            listener.onPasswordConfirmed(null);
        });
        builder.setIcon(R.drawable.ic_dialog_alert);
        builder.show();
    }

    interface PasswordConfirmationListener {
        void onPasswordConfirmed(String password);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    viewModel.logout();
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
        accountDeletionNavigator = null;
        logoutNavigator = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}