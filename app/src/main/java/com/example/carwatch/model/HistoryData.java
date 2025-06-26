package com.example.carwatch.model;

import com.google.gson.annotations.SerializedName;

public class HistoryData {
    @SerializedName("subject")
    private String subject;

    @SerializedName("plate")
    private String plate;

    @SerializedName("description")
    private String description;

    @SerializedName("date")
    private String date;

    @SerializedName("image_id")
    private Integer imageId;

    // Getters
    public String getSubject() {
        return subject;
    }

    public String getPlate() {
        return plate;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public Integer getImageId() {
        return imageId;
    }
}
