package com.example.carwatch.network;

import com.example.carwatch.model.ServerResponse;
import com.example.carwatch.model.LoginResponse;
import com.example.carwatch.model.RegisterResponse;
import com.example.carwatch.model.HistoryResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/update/username")
    Call<ServerResponse> updateUser(@Body Map<String, String> body);

    @POST("auth/update/password")
    Call<ServerResponse> updatePassword(@Body Map<String, String> body);

    @POST("auth/delete")
    Call<ServerResponse> deleteUser(@Body Map<String, String> body);

    @POST("auth/login")
    Call<LoginResponse> login(@Body Map<String, String> body);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body Map<String, String> body);

    @POST("auth/logout")
    Call<ServerResponse> logout();

    @GET("api/history")
    Call<HistoryResponse> getHistory();
}

