package com.oasis.backend.source.mangadex;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sources/mangadex")
public class MangaDexController {

    private final MangaDexService mangaDexService;

    public MangaDexController(MangaDexService mangaDexService) {
        this.mangaDexService = mangaDexService;
    }

    @GetMapping("/series")
    public ResponseEntity<?> getSeries(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contentRating,
            @RequestParam(required = false) String order
    ) {
        return mangaDexService.getSeries(query, limit, status, contentRating, order);
    }

    @GetMapping("/series/{mangaId}")
    public ResponseEntity<?> getSeriesDetails(@PathVariable String mangaId) {
        return mangaDexService.getSeriesDetails(mangaId);
    }

    @GetMapping("/series/{mangaId}/chapters")
    public ResponseEntity<?> getChapters(@PathVariable String mangaId) {
        return mangaDexService.getChapters(mangaId);
    }

    @GetMapping("/series/{mangaId}/chapters/{chapterId}/pages")
    public ResponseEntity<?> getChapterPages(
            @PathVariable String mangaId,
            @PathVariable String chapterId
    ) {
        return mangaDexService.getChapterPages(chapterId);
    }

    @GetMapping("/series/{mangaId}/chapters/{chapterId}/navigation")
    public ResponseEntity<?> getChapterNavigation(
            @PathVariable String mangaId,
            @PathVariable String chapterId
    ) {
        return mangaDexService.getChapterNavigation(mangaId, chapterId);
    }

    @GetMapping("/image")
    public ResponseEntity<?> proxyImage(@RequestParam String url) {
        return mangaDexService.proxyImage(url);
    }
}
