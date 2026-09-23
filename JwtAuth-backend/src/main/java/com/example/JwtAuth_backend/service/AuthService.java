package com.example.JwtAuth_backend.service;

import com.example.JwtAuth_backend.dto.LoginRequest;
import com.example.JwtAuth_backend.dto.LoginResponse;
import com.example.JwtAuth_backend.dto.RefreshTokenRequest;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;


public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshAccessToken(RefreshTokenRequest request);

    LoginResponse oauth2Login(String username);

    void logout(RefreshTokenRequest request);
}
