package com.oasis.backend.profile.uploadphoto.controller;

import com.oasis.backend.profile.uploadphoto.service.UploadPhotoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class UploadPhotoController {

    private final UploadPhotoService uploadPhotoService;

    public UploadPhotoController(UploadPhotoService uploadPhotoService) {
        this.uploadPhotoService = uploadPhotoService;
    }

    @PostMapping(value = "/{userId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file
    ) {
        return uploadPhotoService.uploadPhoto(userId, file);
    }
}