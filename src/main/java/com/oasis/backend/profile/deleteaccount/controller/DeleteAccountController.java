package com.oasis.backend.profile.deleteaccount.controller;

import com.oasis.backend.profile.deleteaccount.service.DeleteAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class DeleteAccountController {

    private final DeleteAccountService deleteAccountService;

    public DeleteAccountController(DeleteAccountService deleteAccountService) {
        this.deleteAccountService = deleteAccountService;
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable String userId) {
        return deleteAccountService.deleteAccount(userId);
    }
}
