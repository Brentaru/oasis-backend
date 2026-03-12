package com.oasis.backend.auth.login.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String message
) {
}