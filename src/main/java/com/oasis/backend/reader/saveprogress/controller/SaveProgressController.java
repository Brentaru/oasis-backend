package com.oasis.backend.reader.saveprogress.controller;

import com.oasis.backend.reader.saveprogress.dto.SaveProgressRequest;
import com.oasis.backend.reader.saveprogress.service.SaveProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader/progress")
public class SaveProgressController {

    private final SaveProgressService saveProgressService;

    public SaveProgressController(SaveProgressService saveProgressService) {
        this.saveProgressService = saveProgressService;
    }

    @PutMapping("/{userId}/{seriesId}")
    public ResponseEntity<?> saveProgress(
            @PathVariable String userId,
            @PathVariable String seriesId,
            @RequestBody SaveProgressRequest req
    ) {
        return saveProgressService.saveProgress(userId, seriesId, req);
    }
}
