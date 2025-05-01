package com.example.carwatch.ui.history;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

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
        // Wrap the current context with your AppTheme
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

        // Set the etDate field to today's date upon fragment creation
        setCurrentDate();
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
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class); // Removed the factory
        viewModel.getHistoryItems().observe(getViewLifecycleOwner(), historyItems -> {
            updateUI(historyItems);
        });
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(viewModel.getHistoryItems().getValue());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupDatePicker() {
        // Set up date picker for end icon and edit text
        dateInputLayout.setEndIconOnClickListener(v -> showDatePickerDialog());
        etDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    String selectedDate = sdf.format(calendar.getTime());
                    etDate.setText(selectedDate);
                    // **Remove** filtering here
                    // viewModel.filterHistoryByDate(selectedDate);
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
            viewModel.filterHistoryByDate(selectedDate);
        });

        btnClear.setOnClickListener(v -> {
                    viewModel.clearHistory();
                    updateUI(new ArrayList<HistoryViewModel.HistoryItem>());
                }
        );

        btnToday.setOnClickListener(v -> {
            setCurrentDate();
        });
    }

    private void updateUI(List<HistoryViewModel.HistoryItem> historyItems) {
        if (historyItems == null || historyItems.isEmpty()) {
            tvNoActivity.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoActivity.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateData(historyItems);
        }
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String today = sdf.format(Calendar.getInstance().getTime());
        etDate.setText(today);
        viewModel.filterHistoryByDate(today);
    }
}