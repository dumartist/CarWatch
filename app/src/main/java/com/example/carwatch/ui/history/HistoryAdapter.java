package com.example.carwatch.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwatch.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<HistoryViewModel.HistoryItem> historyItems;

    public HistoryAdapter(List<HistoryViewModel.HistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryViewModel.HistoryItem item = historyItems.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvTimestamp.setText(item.getTimestamp());
        holder.tvDetails.setText(item.getDetails());
        holder.tvCarNumber.setText(item.getPlate());
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void updateData(List<HistoryViewModel.HistoryItem> newItems) {
        this.historyItems = newItems;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTimestamp, tvDetails, tvCarNumber;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvTimestamp = itemView.findViewById(R.id.tv_history_timestamp);
            tvDetails = itemView.findViewById(R.id.tv_history_details);
            tvCarNumber = itemView.findViewById(R.id.tv_history_car_number);
        }
    }
}