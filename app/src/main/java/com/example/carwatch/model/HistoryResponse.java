package com.example.carwatch.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HistoryResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<HistoryData> data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<HistoryData> getData() {
        return data;
    }
}
