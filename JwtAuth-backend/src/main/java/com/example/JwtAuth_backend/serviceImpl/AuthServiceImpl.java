package com.example.JwtAuth_backend.serviceImpl;


import com.example.JwtAuth_backend.dto.LoginRequest;
import com.example.JwtAuth_backend.dto.LoginResponse;
import com.example.JwtAuth_backend.dto.RefreshTokenRequest;
import com.example.JwtAuth_backend.exception.InvalidCredentialsException;
import com.example.JwtAuth_backend.model.RefreshToken;
import com.example.JwtAuth_backend.model.User;
import com.example.JwtAuth_backend.repository.RefreshTokenRepository;
import com.example.JwtAuth_backend.repository.UserRepository;
import com.example.JwtAuth_backend.service.AuthService;
import com.example.JwtAuth_backend.util.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;
    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository
                .findByUsername(loginRequest.getUsername())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid username or password"
                        ));

        // Google account has no local password
        if (user.getPassword() == null) {

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        boolean passwordMatches =
                BCrypt.checkpw(
                        loginRequest.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        return generateLoginResponse(user);
    }

    private String createRefreshToken(String username) {

        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setUsername(username);

        refreshToken.setExpiryDate(
                LocalDateTime.now()
                        .plusSeconds(refreshExpiration / 1000)
        );

        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Override
    public LoginResponse refreshAccessToken(
            RefreshTokenRequest request) {

        // 1. Find refresh token in database
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid refresh token"
                                ));

        // 2. Check whether token is revoked
        if (refreshToken.isRevoked()) {
            throw new RuntimeException(
                    "Refresh token has been revoked"
            );
        }

        // 3. Check whether token is expired
        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Refresh token has expired"
            );
        }

        // 4. Generate new access token
        String newAccessToken =
                jwtUtil.generateToken(
                        refreshToken.getUsername()
                );

        // 5. Return new access token
        return new LoginResponse(
                newAccessToken,
                refreshToken.getToken(),
                refreshToken.getUsername()
        );
    }

    // ==========================================
    // OAUTH2 LOGIN
    // ==========================================

    @Override
    public LoginResponse oauth2Login(String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "OAuth2 user not found"
                        ));

        return generateLoginResponse(user);
    }

    // ==========================================
    // COMMON TOKEN GENERATION
    // ==========================================

    private LoginResponse generateLoginResponse(User user) {

        String accessToken =
                jwtUtil.generateToken(user.getUsername());

        String refreshToken =
                createRefreshToken(user.getUsername());

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUsername()
        );
    }


    @Override
    public void logout(RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid refresh token"
                                ));

        // Revoke refresh token
        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
}