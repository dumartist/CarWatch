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

    @POST("user/update/username") // Endpoint to update username
    Call<ServerResponse> updateUser(
            @Body Map<String, String> body
    );

    @POST("user/update/password") // Endpoint to update password
    Call<ServerResponse> updatePassword(
            @Body Map<String, String> body
    );

    @POST("user/delete") // Endpoint to delete user account
    Call<ServerResponse> deleteUser(
            @Body Map<String, String> body
    );

    @POST("login")
    Call<LoginResponse> login(@Body Map<String, String> body);

    @POST("register")
    Call<RegisterResponse> register(@Body Map<String, String> body);

    @POST("logout") // Endpoint for server-side logout
    Call<ServerResponse> logout();
    
    @GET("history/get") // Endpoint to fetch all history for the logged-in user (Flask uses session)
    Call<HistoryResponse> getHistory();

}
