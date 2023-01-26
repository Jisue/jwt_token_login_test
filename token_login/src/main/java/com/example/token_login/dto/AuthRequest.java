package com.example.token_login.dto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthRequest {
    private String accessToken;
    private String refreshToken;
}
