package com.example.carwatch.ui.login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.carwatch.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private ViewSwitcher viewSwitcher;
    private EditText etLoginUsername, etLoginPassword;
    private EditText etSignUpUsername, etSignUpPassword, etConfirmPassword;

    private LoginViewModel loginViewModel;

    public interface OnLoginSuccessListener {
        void onLoginSuccess(String userId, String username);
    }

    private OnLoginSuccessListener loginSuccessListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewSwitcher = binding.viewSwitcher;

        TextView tvCreateAccount = binding.tvCreateAccount;
        TextView tvSignIn = binding.tvSignIn;

        etLoginUsername = binding.etLoginUsername;
        etLoginPassword = binding.etLoginPassword;
        Button btnSignIn = binding.btnSignIn;

        etSignUpUsername = binding.etSignUpUsername;
        etSignUpPassword = binding.etSignUpPassword;
        etConfirmPassword = binding.etConfirmPassword;
        Button btnCreateAccount = binding.btnCreateAccount;

        tvCreateAccount.setOnClickListener(v -> viewSwitcher.showNext());

        tvSignIn.setOnClickListener(v -> viewSwitcher.showPrevious());

        btnSignIn.setOnClickListener(v -> {
            String username = etLoginUsername.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();
            if (!username.isEmpty() && !password.isEmpty()) {
                loginViewModel.login(username, password);
            } else {
                Toast.makeText(getActivity(), "Username atau Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreateAccount.setOnClickListener(v -> {
            String username = etSignUpUsername.getText().toString().trim();
            String password = etSignUpPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Password tidak cocok!", Toast.LENGTH_SHORT).show();
            } else {
                loginViewModel.register(username, password);
            }
        });

        setupObservers();

        return root;
    }

    private void setupObservers() {
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            if (loginResult == null) return;

            if (loginResult.getSuccessData() != null) {
                String userId = String.valueOf(loginResult.getSuccessData().getUserId());
                String username = loginResult.getSuccessData().getUsername();

                if (loginSuccessListener != null) {
                    loginSuccessListener.onLoginSuccess(userId, username);
                } else {
                    Log.e("LoginFragment", "OnLoginSuccessListener is null");
                }
            } else if (loginResult.getError() != null) {
                Toast.makeText(getActivity(), loginResult.getError(), Toast.LENGTH_LONG).show();
                Log.e("LoginFragment", "Login failed: " + loginResult.getError());
            }
        });

        loginViewModel.getRegisterResult().observe(getViewLifecycleOwner(), registerResult -> {
            if (registerResult == null) return;

            Toast.makeText(getActivity(), registerResult.getMessage(), Toast.LENGTH_SHORT).show();
            if (registerResult.isSuccess()) {
                viewSwitcher.showPrevious();
                etSignUpUsername.setText("");
                etSignUpPassword.setText("");
                etConfirmPassword.setText("");
            } else {
                 Log.e("LoginFragment", "Registration failed: " + registerResult.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginSuccessListener) {
            loginSuccessListener = (OnLoginSuccessListener) context;
        } else {
            throw new ClassCastException(context
                    + " must implement LoginFragment.OnLoginSuccessListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginSuccessListener = null;
    }
}