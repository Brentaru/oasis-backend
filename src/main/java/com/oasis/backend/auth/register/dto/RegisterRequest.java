package com.oasis.backend.auth.register.dto;

public record RegisterRequest(String email, String password, String confirmPassword) {}