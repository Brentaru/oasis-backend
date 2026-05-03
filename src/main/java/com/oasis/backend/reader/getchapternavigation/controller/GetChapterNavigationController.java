package com.oasis.backend.reader.getchapternavigation.controller;

import com.oasis.backend.reader.getchapternavigation.service.GetChapterNavigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader")
public class GetChapterNavigationController {

    private final GetChapterNavigationService getChapterNavigationService;

    public GetChapterNavigationController(GetChapterNavigationService getChapterNavigationService) {
        this.getChapterNavigationService = getChapterNavigationService;
    }

    @GetMapping("/series/{seriesId}/chapters/{chapterId}/navigation")
    public ResponseEntity<?> getChapterNavigation(
            @PathVariable String seriesId,
            @PathVariable String chapterId
    ) {
        return getChapterNavigationService.getChapterNavigation(seriesId, chapterId);
    }
}
