package com.example.JwtAuth_backend.security;

import com.example.JwtAuth_backend.dto.LoginResponse;
import com.example.JwtAuth_backend.model.AuthProvider;
import com.example.JwtAuth_backend.model.User;
import com.example.JwtAuth_backend.repository.UserRepository;
import com.example.JwtAuth_backend.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // Google authenticated user
        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();


        // -----------------------------------------
        // Get Google user information
        // -----------------------------------------

        String providerId =
                oauth2User.getAttribute("sub");

        String email =
                oauth2User.getAttribute("email");

        String name =
                oauth2User.getAttribute("name");


        if (providerId == null ||
                email == null ||
                email.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Required Google user information not received"
            );

            return;
        }


        // -----------------------------------------
        // Find existing user
        // -----------------------------------------

        User user =
                userRepository
                        .findByProviderAndProviderId(
                                AuthProvider.GOOGLE,
                                providerId
                        )
                        .orElse(null);


        // -----------------------------------------
        // If not found, check username/email
        // -----------------------------------------

        if (user == null) {

            user =
                    userRepository
                            .findByUsername(email)
                            .orElse(null);
        }


        // -----------------------------------------
        // Create Google user
        // -----------------------------------------

        if (user == null) {

            user = new User();

            user.setUsername(email);

            // Google users don't have local password
            user.setPassword(null);

            user.setProvider(
                    AuthProvider.GOOGLE
            );

            user.setProviderId(providerId);

            user =
                    userRepository.save(user);
        }


        // -----------------------------------------
        // Existing local user linked with Google
        // -----------------------------------------

        else {

            if (user.getProvider() == null) {
                user.setProvider(
                        AuthProvider.GOOGLE
                );
            }

            if (user.getProviderId() == null) {
                user.setProviderId(providerId);
            }

            user =
                    userRepository.save(user);
        }


        // -----------------------------------------
        // Generate OUR JWT + Refresh Token
        // -----------------------------------------

        LoginResponse loginResponse =
                authService.oauth2Login(
                        user.getUsername()
                );


        // -----------------------------------------
        // Encode values
        // -----------------------------------------

        String accessToken =
                URLEncoder.encode(
                        loginResponse.getToken(),
                        StandardCharsets.UTF_8
                );

        String refreshToken =
                URLEncoder.encode(
                        loginResponse.getRefreshToken(),
                        StandardCharsets.UTF_8
                );

        String username =
                URLEncoder.encode(
                        loginResponse.getUsername(),
                        StandardCharsets.UTF_8
                );


        // -----------------------------------------
        // Redirect to Angular
        // -----------------------------------------

        String targetUrl =
                redirectUri
                        + "#accessToken=" + accessToken
                        + "&refreshToken=" + refreshToken
                        + "&username=" + username;

        response.sendRedirect(targetUrl);
    }
}