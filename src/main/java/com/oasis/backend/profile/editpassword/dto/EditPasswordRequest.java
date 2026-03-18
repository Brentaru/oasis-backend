package com.oasis.backend.profile.editpassword.dto;

public record EditPasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {
}
