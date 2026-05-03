package com.oasis.backend.library.getseriesdetails.controller;

import com.oasis.backend.library.getseriesdetails.service.GetSeriesDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class GetSeriesDetailsController {

    private final GetSeriesDetailsService getSeriesDetailsService;

    public GetSeriesDetailsController(GetSeriesDetailsService getSeriesDetailsService) {
        this.getSeriesDetailsService = getSeriesDetailsService;
    }

    @GetMapping("/series/{seriesId}")
    public ResponseEntity<?> getSeriesDetails(@PathVariable String seriesId) {
        return getSeriesDetailsService.getSeriesDetails(seriesId);
    }
}

