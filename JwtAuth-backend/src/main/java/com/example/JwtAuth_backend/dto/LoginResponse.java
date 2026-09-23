package com.example.JwtAuth_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponse {

    private String token;
    private String refreshToken;
    private String username;

}
