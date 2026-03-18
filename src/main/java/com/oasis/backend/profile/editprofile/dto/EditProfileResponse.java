package com.oasis.backend.profile.editprofile.dto;

public record EditProfileResponse(
        String userId,
        String fullName,
        String email,
        String phone,
        String bio,
        String profilePhoto,
        String message
) {
}
