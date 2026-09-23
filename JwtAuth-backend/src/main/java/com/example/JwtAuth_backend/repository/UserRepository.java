package com.example.JwtAuth_backend.repository;

import com.example.JwtAuth_backend.model.AuthProvider;
import com.example.JwtAuth_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByProviderAndProviderId(
            AuthProvider provider,
            String providerId
    );
}
