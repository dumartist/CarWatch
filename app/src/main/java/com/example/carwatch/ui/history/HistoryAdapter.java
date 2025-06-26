package com.example.carwatch.ui.history;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.carwatch.R;

import java.util.List;

public class HistoryAdapter extends ListAdapter<HistoryViewModel.UiHistoryItem, HistoryAdapter.HistoryViewHolder> {

    private static final String TAG = "HistoryAdapter";
    private static final String BASE_IMAGE_URL = "http://192.168.18.9:8000/api/get_image/";

    public HistoryAdapter() {
        super(new HistoryDiffCallback());
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
        HistoryViewModel.UiHistoryItem item = getItem(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvTimestamp.setText(item.getTimestamp());
        holder.tvDetails.setText(item.getDetails());
        holder.tvCarNumber.setText(item.getPlate());

        if (item.getImageId() != null) {
            String imageUrl = BASE_IMAGE_URL + item.getImageId();
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_dialog_alert)
                    .error(R.drawable.ic_dialog_alert)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide load failed for URL: " + model, e);
                            return false; // Allow fallback error drawable to be shown
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_dialog_alert);
        }
    }

    public void updateData(List<HistoryViewModel.UiHistoryItem> newItems) {
        submitList(newItems);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTimestamp, tvDetails, tvCarNumber;
        ImageView imageView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvTimestamp = itemView.findViewById(R.id.tv_history_timestamp);
            tvDetails = itemView.findViewById(R.id.tv_history_details);
            tvCarNumber = itemView.findViewById(R.id.tv_history_car_number);
            imageView = itemView.findViewById(R.id.history_preview);
        }
    }
}
