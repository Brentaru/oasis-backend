package com.oasis.backend.profile.editpassword.controller;

import com.oasis.backend.profile.editpassword.dto.EditPasswordRequest;
import com.oasis.backend.profile.editpassword.service.EditPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class EditPasswordController {

    private final EditPasswordService editPasswordService;

    public EditPasswordController(EditPasswordService editPasswordService) {
        this.editPasswordService = editPasswordService;
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> editPassword(@PathVariable String userId, @RequestBody EditPasswordRequest req) {
        return editPasswordService.editPassword(userId, req);
    }
}
