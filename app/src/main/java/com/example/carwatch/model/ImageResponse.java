package com.example.carwatch.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class ImageResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("filename")
    private String filename;

    @SerializedName("filepath")
    private String filepath;

    @SerializedName("upload_date")
    private String uploadDate;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    @NonNull
    @Override
    public String toString() {
        return "ImageResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", filename='" + filename + '\'' +
                ", filepath='" + filepath + '\'' +
                ", uploadDate='" + uploadDate + '\'' +
                '}';
    }
}
