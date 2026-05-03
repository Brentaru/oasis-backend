package com.oasis.backend.reader.getprogress.controller;

import com.oasis.backend.reader.getprogress.service.GetProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader/progress")
public class GetProgressController {

    private final GetProgressService getProgressService;

    public GetProgressController(GetProgressService getProgressService) {
        this.getProgressService = getProgressService;
    }

    @GetMapping("/{userId}/{seriesId}")
    public ResponseEntity<?> getProgress(
            @PathVariable String userId,
            @PathVariable String seriesId
    ) {
        return getProgressService.getProgress(userId, seriesId);
    }
}
