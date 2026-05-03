package com.oasis.backend.library.searchseries.controller;

import com.oasis.backend.library.searchseries.service.SearchSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class SearchSeriesController {

    private final SearchSeriesService searchSeriesService;

    public SearchSeriesController(SearchSeriesService searchSeriesService) {
        this.searchSeriesService = searchSeriesService;
    }

    @GetMapping("/series/search")
    public ResponseEntity<?> searchSeries(@RequestParam String keyword) {
        return searchSeriesService.searchSeries(keyword);
    }
}

