package com.example.carwatch.ui.history;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwatch.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryFragment extends Fragment {
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;
    private TextView tvNoActivity;
    private RecyclerView recyclerView;
    private TextInputEditText etDate;
    private TextInputLayout dateInputLayout;
    private Button btnSearch, btnClear, btnToday;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context themedContext = new ContextThemeWrapper(requireContext(), R.style.AppTheme);
        LayoutInflater themedInflater = inflater.cloneInContext(themedContext);
        return themedInflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupDatePicker();
        setupButtons();

        setCurrentDateAndFetch();
    }

    private void initializeViews(View view) {
        tvNoActivity = view.findViewById(R.id.tv_no_activity);
        recyclerView = view.findViewById(R.id.recycler_view_history);
        etDate = view.findViewById(R.id.et_date);
        dateInputLayout = view.findViewById(R.id.date_input_layout);
        btnSearch = view.findViewById(R.id.btn_search);
        btnClear = view.findViewById(R.id.btn_clear);
        btnToday = view.findViewById(R.id.btn_today);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        viewModel.getHistoryUiItems().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupDatePicker() {
        dateInputLayout.setEndIconOnClickListener(v -> showDatePickerDialog());
        etDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setCurrentDateAndFetch() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"));
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        String today = sdf.format(calendar.getTime());
        etDate.setText(today);
        viewModel.filterHistoryByDate(today);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"));
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
                    String selectedDate = sdf.format(calendar.getTime());
                    etDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupButtons() {
        btnSearch.setOnClickListener(v -> {
            String selectedDate = etDate.getText().toString();
            if (selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a date.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.filterHistoryByDate(selectedDate);
        });

        btnClear.setOnClickListener(v -> {
            viewModel.clearDisplayedHistory();
            etDate.setText("");
        });

        btnToday.setOnClickListener(v -> {
            setCurrentDateAndFetch();
        });
    }

    private void updateUI(List<HistoryViewModel.UiHistoryItem> uiHistoryItems) {
        if (uiHistoryItems == null || uiHistoryItems.isEmpty()) {
            tvNoActivity.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoActivity.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.updateData(uiHistoryItems);
    }
}