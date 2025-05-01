package com.example.carwatch.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private ViewSwitcher viewSwitcher;
    private EditText etLoginUsername, etLoginPassword;
    private EditText etSignUpUsername, etSignUpPassword, etConfirmPassword;
    private Button btnSignIn, btnCreateAccount;

    public interface OnLoginSuccessListener {
        void onLoginSuccess(String username);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LoginViewModel LoginViewModel =
                new ViewModelProvider(this).get(LoginViewModel.class);

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewSwitcher = binding.viewSwitcher;

        TextView tvCreateAccount = binding.tvCreateAccount;
        TextView tvSignIn = binding.tvSignIn;

        // Form Login
        etLoginUsername = binding.etLoginUsername;
        etLoginPassword = binding.etLoginPassword;
        btnSignIn = binding.btnSignIn;

        // Form Sign-Up
        etSignUpUsername = binding.etSignUpUsername;
        etSignUpPassword = binding.etSignUpPassword;
        etConfirmPassword = binding.etConfirmPassword;
        btnCreateAccount = binding.btnCreateAccount;

        // Jika klik "Create Account", pindah ke form sign-up
        tvCreateAccount.setOnClickListener(v -> viewSwitcher.showNext());

        // Jika klik "Sign In", kembali ke form login
        tvSignIn.setOnClickListener(v -> viewSwitcher.showPrevious());

        btnSignIn.setOnClickListener(v -> {
            String username = etLoginUsername.getText().toString();
            String password = etLoginPassword.getText().toString();
            if (!username.isEmpty() && !password.isEmpty()) {
                new LoginTask().execute(username, password);
            } else {
                Toast.makeText(getActivity(), "Username atau Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreateAccount.setOnClickListener(v -> {
            String username = etSignUpUsername.getText().toString();
            String password = etSignUpPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Password tidak cocok!", Toast.LENGTH_SHORT).show();
            } else {
                new RegisterTask().execute(username, password);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://192.168.1.4/carwatch/login.php"); // Ganti dengan IP server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("username=" + params[0] + "&password=" + params[1]);
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(result);
                boolean success = jsonResponse.getBoolean("success");

                if (success) {
                    String userId = jsonResponse.getString("user_id");
                    String username = jsonResponse.getString("username");
                    String password = etLoginPassword.getText().toString(); // Get the entered password

                    // Save the username, userId, and password to SharedPreferences
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("my_app", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.putString("userId", userId);
                    editor.putString("password", password);
                    editor.apply(); // Use apply() instead of commit() for asynchronous saving

                    // Call the interface method with the username
                    if (getActivity() instanceof OnLoginSuccessListener) {
                        ((OnLoginSuccessListener) getActivity()).onLoginSuccess(username);
                    } else {
                        Log.e("LoginFragment", "Activity must implement OnLoginSuccessListener");
                    }
                } else {
                    // Handle login failure (e.g., display an error message)
                    Toast.makeText(getActivity(), "Login failed: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error parsing server response", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RegisterTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://192.168.1.4/carwatch/register.php"); // Ganti dengan IP server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("username=" + params[0] + "&password=" + params[1]);
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Just display the raw result
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();

            //Switch to login view if register success
            viewSwitcher.showPrevious();
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginSuccessListener) {
            //Assign the listener
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement LoginFragment.OnLoginSuccessListener");
        }
    }

}