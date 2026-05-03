package com.oasis.backend.browse.filterseries.controller;

import com.oasis.backend.browse.filterseries.service.FilterSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/browse")
public class FilterSeriesController {

    private final FilterSeriesService filterSeriesService;

    public FilterSeriesController(FilterSeriesService filterSeriesService) {
        this.filterSeriesService = filterSeriesService;
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterSeries(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return filterSeriesService.filterSeries(genre, status, keyword);
    }
}
