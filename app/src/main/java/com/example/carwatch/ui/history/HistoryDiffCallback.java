package com.example.carwatch.ui.history;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

public class HistoryDiffCallback extends DiffUtil.ItemCallback<HistoryViewModel.UiHistoryItem> {

    @Override
    public boolean areItemsTheSame(@NonNull HistoryViewModel.UiHistoryItem oldItem, @NonNull HistoryViewModel.UiHistoryItem newItem) {
        return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp());
    }

    @Override
    public boolean areContentsTheSame(@NonNull HistoryViewModel.UiHistoryItem oldItem, @NonNull HistoryViewModel.UiHistoryItem newItem) {
        return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp()) &&
                Objects.equals(oldItem.getDetails(), newItem.getDetails()) &&
                Objects.equals(oldItem.getPlate(), newItem.getPlate());
    }
}