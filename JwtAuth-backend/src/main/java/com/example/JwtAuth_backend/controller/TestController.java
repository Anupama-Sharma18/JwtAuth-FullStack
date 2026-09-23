package com.example.JwtAuth_backend.controller;

import com.example.JwtAuth_backend.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final JwtUtil jwtUtil;

    public TestController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedApi(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(401)
                    .body("Authorization token is required");
        }

        String token = authorizationHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity
                    .status(401)
                    .body("Invalid or expired token");
        }

        String username = jwtUtil.extractUsername(token);

        return ResponseEntity.ok(
                "Access granted. Welcome " + username
        );
    }
}