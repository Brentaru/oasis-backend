package com.oasis.backend.profile.editprofile.dto;

public record EditProfileRequest(
        String fullName,
        String email,
        String phone,
        String bio
) {
}
