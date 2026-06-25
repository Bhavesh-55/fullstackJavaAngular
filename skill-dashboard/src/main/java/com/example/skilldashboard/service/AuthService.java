package com.example.skilldashboard.service;

import com.example.skilldashboard.dto.auth.AuthResponse;
import com.example.skilldashboard.dto.auth.LoginRequest;
import com.example.skilldashboard.dto.auth.SignupRequest;

public interface AuthService {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshAccessToken(String refreshToken);

    void logout(String refreshToken);
}