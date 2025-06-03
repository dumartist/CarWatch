package com.example.carwatch.model;

import com.google.gson.annotations.SerializedName;

public class LoginData {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("username")
    private String username;

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
