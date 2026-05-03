package com.oasis.backend.library.getchapters.controller;

import com.oasis.backend.library.getchapters.service.GetChaptersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class GetChaptersController {

    private final GetChaptersService getChaptersService;

    public GetChaptersController(GetChaptersService getChaptersService) {
        this.getChaptersService = getChaptersService;
    }

    @GetMapping("/series/{seriesId}/chapters")
    public ResponseEntity<?> getChapters(@PathVariable String seriesId) {
        return getChaptersService.getChapters(seriesId);
    }
}

