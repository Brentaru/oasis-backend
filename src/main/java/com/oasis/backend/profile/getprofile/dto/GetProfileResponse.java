package com.oasis.backend.profile.getprofile.dto;

public record GetProfileResponse(
        String userId,
        String fullName,
        String email,
        String profilePhoto
) {
}
