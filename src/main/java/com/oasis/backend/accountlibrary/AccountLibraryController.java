package com.oasis.backend.accountlibrary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account-library")
public class AccountLibraryController {

    private final AccountLibraryService accountLibraryService;

    public AccountLibraryController(AccountLibraryService accountLibraryService) {
        this.accountLibraryService = accountLibraryService;
    }

    @GetMapping("/{userId}/saved")
    public ResponseEntity<?> getSavedTitles(@PathVariable String userId) {
        return accountLibraryService.getSavedTitles(userId);
    }

    @PutMapping("/{userId}/saved")
    public ResponseEntity<?> saveTitle(@PathVariable String userId, @RequestBody Map<String, Object> request) {
        return accountLibraryService.saveTitle(userId, request);
    }

    @DeleteMapping("/{userId}/saved/{seriesId}")
    public ResponseEntity<?> removeSavedTitle(@PathVariable String userId, @PathVariable String seriesId) {
        return accountLibraryService.removeSavedTitle(userId, seriesId);
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<?> getReadingHistory(@PathVariable String userId) {
        return accountLibraryService.getReadingHistory(userId);
    }

    @PutMapping("/{userId}/history")
    public ResponseEntity<?> saveReadingHistory(@PathVariable String userId, @RequestBody Map<String, Object> request) {
        return accountLibraryService.saveReadingHistory(userId, request);
    }
}
