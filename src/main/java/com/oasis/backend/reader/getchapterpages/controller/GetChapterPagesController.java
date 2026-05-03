package com.oasis.backend.reader.getchapterpages.controller;

import com.oasis.backend.reader.getchapterpages.service.GetChapterPagesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader")
public class GetChapterPagesController {

    private final GetChapterPagesService getChapterPagesService;

    public GetChapterPagesController(GetChapterPagesService getChapterPagesService) {
        this.getChapterPagesService = getChapterPagesService;
    }

    @GetMapping("/series/{seriesId}/chapters/{chapterId}/pages")
    public ResponseEntity<?> getChapterPages(
            @PathVariable String seriesId,
            @PathVariable String chapterId
    ) {
        return getChapterPagesService.getChapterPages(seriesId, chapterId);
    }
}
