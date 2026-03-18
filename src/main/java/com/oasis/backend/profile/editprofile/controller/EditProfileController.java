package com.oasis.backend.profile.editprofile.controller;

import com.oasis.backend.profile.editprofile.dto.EditProfileRequest;
import com.oasis.backend.profile.editprofile.service.EditProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class EditProfileController {

    private final EditProfileService editProfileService;

    public EditProfileController(EditProfileService editProfileService) {
        this.editProfileService = editProfileService;
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> editProfile(@PathVariable String userId, @RequestBody EditProfileRequest req) {
        return editProfileService.editProfile(userId, req);
    }
}
