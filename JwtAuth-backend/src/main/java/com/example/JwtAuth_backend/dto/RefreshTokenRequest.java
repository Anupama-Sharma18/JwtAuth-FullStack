package com.example.JwtAuth_backend.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {

    private String refreshToken;
}
