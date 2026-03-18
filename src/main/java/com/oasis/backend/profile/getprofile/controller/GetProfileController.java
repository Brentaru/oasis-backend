package com.oasis.backend.profile.getprofile.controller;

import com.oasis.backend.profile.getprofile.service.GetProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class GetProfileController {

    private final GetProfileService getProfileService;

    public GetProfileController(GetProfileService getProfileService) {
        this.getProfileService = getProfileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        return getProfileService.getProfile(userId);
    }
}
