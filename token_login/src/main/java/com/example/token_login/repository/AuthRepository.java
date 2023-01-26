package com.example.token_login.repository;

import com.example.token_login.domain.Auth;
import com.example.token_login.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByUserId(User user);
}