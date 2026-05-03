package com.oasis.backend.home.continuereading.controller;

import com.oasis.backend.home.continuereading.service.ContinueReadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class ContinueReadingController {

    private final ContinueReadingService continueReadingService;

    public ContinueReadingController(ContinueReadingService continueReadingService) {
        this.continueReadingService = continueReadingService;
    }

    @GetMapping("/continue-reading/{userId}")
    public ResponseEntity<?> getContinueReading(@PathVariable String userId) {
        return continueReadingService.getContinueReading(userId);
    }
}
